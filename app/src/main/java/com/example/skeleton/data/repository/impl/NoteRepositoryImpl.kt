package com.example.skeleton.data.repository.impl

import android.util.Log
import com.example.skeleton.common.Outcome
import com.example.skeleton.data.database.local.dao.NoteDao
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate

/**
 * The notes store, backed by the `notes` table.
 *
 * Room-only: there is no network behind a note and no cache in front of it. Room already keeps one
 * copy of the truth and already pushes a new list whenever the table changes, so adding a second
 * copy here would only create something that can disagree with the database.
 *
 * @param noteDao the table's accessor.
 * @param clock where "now" and "today" come from. A constructor parameter and not a direct
 *   `System.currentTimeMillis()` / `LocalDate.now()` call, because two of this class's promises —
 *   that a new note is stamped with the current time, and that a note dated after today is
 *   refused — are only checkable if a test can decide what time it is. One `Clock` rather than two
 *   lambdas so that the millisecond it stamps and the day it compares against can never come from
 *   two different readings.
 * @param ioDispatcher where the reading, writing and mapping happen. Also a constructor parameter
 *   so a test can hand over a dispatcher it controls.
 * @author Phong-Kaster
 */
class NoteRepositoryImpl(
    private val noteDao: NoteDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : NoteRepository {

    override val notesFlow: Flow<List<Note>> = noteDao.observeAll()
        .map { entities ->
            entities
                .map { entity -> entity.toDomain() }
                .sortedWith(NEWEST_FIRST)
        }
        // A repository does not throw at the screen above it. If the database itself fails — a
        // corrupt file, a disk that will not read — the stream would otherwise carry that
        // exception up into the ViewModel's collector, where nothing is catching it, and the app
        // would die instead of showing an empty list. "No notes" is the wrong answer, but it is a
        // survivable one, and the log line is what says which happened.
        .catch { throwable ->
            Log.e(TAG, "notesFlow failed; showing an empty list", throwable)
            emit(emptyList())
        }
        .flowOn(ioDispatcher)

    override fun notesForDateFlow(date: LocalDate): Flow<List<Note>> =
        // `toEpochDay()` and not a formatted string: the column is a plain `Long`, and the DAO's
        // `WHERE date = :epochDay` compares numbers. This one line is the whole of what the
        // repository contributes to the query, which makes it the whole of what can go wrong —
        // an off-by-one here shows yesterday's notes under today's heading, with every other
        // test in the app still green.
        noteDao.observeByDate(epochDay = date.toEpochDay())
            .map { entities ->
                entities
                    .map { entity -> entity.toDomain() }
                    .sortedWith(NEWEST_FIRST)
            }
            // Same reasoning as `notesFlow` above: a database that will not read must reach the
            // screen as an empty day, not as an exception thrown into a collector that catches
            // nothing.
            .catch { throwable ->
                Log.e(TAG, "notesForDateFlow($date) failed; showing an empty list", throwable)
                emit(emptyList())
            }
            .flowOn(ioDispatcher)

    override suspend fun getNote(id: Long): Outcome<Note?> = withContext(ioDispatcher) {
        try {
            // A row that is not there is a `Success(null)`, not an error. The database answered
            // the question; the answer was "no such note".
            Outcome.Success(noteDao.getById(id = id)?.toDomain())
        } catch (e: CancellationException) {
            // Re-thrown, never swallowed: a cancelled read is the screen going away, not a
            // failure, and turning it into any kind of answer would be inventing one.
            throw e
        } catch (e: Exception) {
            // And this is an error rather than `Success(null)`, which is the distinction the whole
            // return type exists for. "I could not look" and "I looked, it is gone" lead to
            // different behaviour on the screen above, and a single `null` made them the same.
            Log.e(TAG, "getNote($id) failed", e)
            Outcome.Error(message = "The note could not be read.", throwable = e)
        }
    }

    override suspend fun save(note: Note): Outcome<Unit> = withContext(ioDispatcher) {
        val today = LocalDate.now(clock)

        // The future-date rule, from `knowledge/DOMAIN.md`. `isAfter` and not `>=` on purpose:
        // today is allowed, tomorrow is not. An off-by-one in this direction makes it impossible
        // to write a note at all, which is why the domain rule spells the boundary out.
        if (note.date.isAfter(today)) {
            Log.w(TAG, "save refused: ${note.date} is after $today")
            return@withContext Outcome.Error(message = "A note cannot be dated after $today.")
        }

        val now = clock.millis()
        val stamped = note.copy(
            // A note that has never been stored gets both stamps now. A note that has been stored
            // before keeps the `createdAt` it already has — clobbering it here is the exact bug
            // that makes "created" and "last edited" the same date for every note in the app, and
            // nothing on a screen would show it.
            createdAt = if (note.createdAt == Note.UNSAVED_AT) now else note.createdAt,
            updatedAt = now,
        )

        try {
            noteDao.upsert(note = stamped.toEntity())
            Outcome.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "save(id=${note.id}) failed", e)
            Outcome.Error(message = "The note could not be saved.", throwable = e)
        }
    }

    override suspend fun delete(note: Note): Outcome<Unit> = withContext(ioDispatcher) {
        // A note that was never stored has no row to remove, and Room's `@Delete` would match
        // nothing and report success. The screen above would then tell the user their note was
        // deleted while it sat on Home untouched — so the refusal happens here, where every
        // caller reaches it, exactly like the future-date rule above.
        if (note.id == Note.UNSAVED_ID) {
            Log.w(TAG, "delete refused: the note has never been stored")
            return@withContext Outcome.Error(message = "A note that was never saved cannot be deleted.")
        }

        try {
            // The count is the whole point of asking. Room's `@Delete` matches on the primary key
            // and is perfectly content to match nothing — so a note deleted a moment ago on
            // another surface, or a stale id from a list that has not caught up, would come back
            // here as a success and the screen would say "Note deleted" about a row that was
            // already gone. The sentinel check above catches only *one* way of having no row;
            // this catches all of them.
            val removed = noteDao.delete(note = note.toEntity())
            if (removed == 0) {
                Log.w(TAG, "delete(id=${note.id}) matched no row")
                return@withContext Outcome.Error(message = "There was no such note to delete.")
            }

            Outcome.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "delete(id=${note.id}) failed", e)
            Outcome.Error(message = "The note could not be deleted.", throwable = e)
        }
    }

    companion object {

        private const val TAG = "NoteRepositoryImpl"


        /*
         * --- Why sort here when the SQL already says ORDER BY (simple story) ---
         *
         * `NoteDao.observeAll()` orders in the database, and this sorts the same list again. That
         * looks like waste, and it is on purpose.
         *
         * "Newest first" is a promise this store makes to everything above it, and a promise that
         * lives only inside a SQL string is one that a future query can drop without anything
         * noticing — no compiler error, no failing test, just a list that is subtly in the wrong
         * order. Sorted here, the promise belongs to the repository, and an ordinary JVM test can
         * hold it to it with a fake DAO that hands back a deliberately jumbled list.
         *
         * The cost is sorting a list that is already sorted, on a screenful of notes. The `id`
         * tiebreaker matches the DAO's, so the two orderings can never disagree.
         */
        private val NEWEST_FIRST: Comparator<Note> =
            compareByDescending(Note::updatedAt).thenByDescending(Note::id)
    }
}
