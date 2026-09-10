package com.example.skeleton.data.repository.impl

import android.util.Log
import com.example.skeleton.data.database.local.dao.NoteDao
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

/**
 * The notes store, backed by the `notes` table.
 *
 * Room-only: there is no network behind a note and no cache in front of it. Room already keeps one
 * copy of the truth and already pushes a new list whenever the table changes, so adding a second
 * copy here would only create something that can disagree with the database.
 *
 * @param noteDao the table's accessor.
 * @param ioDispatcher where the reading and mapping happen. A constructor parameter rather than a
 *   hardcoded `Dispatchers.IO` so a test can hand over a dispatcher it controls.
 * @author Phong-Kaster
 */
class NoteRepositoryImpl(
    private val noteDao: NoteDao,
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
