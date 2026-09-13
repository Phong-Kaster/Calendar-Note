package com.example.skeleton.data.repository.impl

import android.util.Log
import com.example.skeleton.common.Outcome
import com.example.skeleton.data.database.local.dao.AlarmDao
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.model.BlankAlarmMessageException
import com.example.skeleton.domain.repository.AlarmRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Clock

/**
 * The alarms store, backed by the `alarms` table.
 *
 * Room-only: there is no network behind an alarm and no cache in front of it. Room already keeps
 * one copy of the truth and already pushes a new list whenever the table changes, so adding a
 * second copy here would only create something that can disagree with the database.
 *
 * @param alarmDao the table's accessor.
 * @param clock where "now" comes from. A constructor parameter and not a direct
 *   `System.currentTimeMillis()` call, because this class's promise — that a new alarm is stamped
 *   with the current time and an existing one keeps its stamp — is only checkable if a test can
 *   decide what time it is. The same seam `NoteRepositoryImpl` uses, for the same reason.
 * @param ioDispatcher where the reading, writing and mapping happen. Also a constructor parameter
 *   so a test can hand over a dispatcher it controls.
 * @author Phong-Kaster
 */
class AlarmRepositoryImpl(
    private val alarmDao: AlarmDao,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : AlarmRepository {

    override val alarmsFlow: Flow<List<Alarm>> = alarmDao.observeAll()
        .map { entities ->
            entities
                .map { entity -> entity.toDomain() }
                .sortedWith(EARLIEST_FIRST)
        }
        // A repository does not throw at the screen above it. If the database itself fails — a
        // corrupt file, a disk that will not read — the stream would otherwise carry that
        // exception up into the ViewModel's collector, where nothing is catching it, and the app
        // would die instead of showing an empty list. "No alarms" is the wrong answer, but it is a
        // survivable one, and the log line is what says which happened.
        .catch { throwable ->
            Log.e(TAG, "alarmsFlow failed; showing an empty list", throwable)
            emit(emptyList())
        }
        .flowOn(ioDispatcher)

    override suspend fun getAlarm(id: Long): Outcome<Alarm?> = withContext(ioDispatcher) {
        try {
            // A row that is not there is a `Success(null)`, not an error. The database answered
            // the question; the answer was "no such alarm".
            Outcome.Success(alarmDao.getById(id = id)?.toDomain())
        } catch (e: CancellationException) {
            // Re-thrown, never swallowed: a cancelled read is the screen going away, not a
            // failure, and turning it into any kind of answer would be inventing one.
            throw e
        } catch (e: Exception) {
            // And this is an error rather than `Success(null)`, which is the distinction the whole
            // return type exists for. "I could not look" and "I looked, it is gone" lead to
            // different behaviour on the screen above, and a single `null` made them the same.
            Log.e(TAG, "getAlarm($id) failed", e)
            Outcome.Error(message = "The alarm could not be read.", throwable = e)
        }
    }

    override suspend fun save(alarm: Alarm): Outcome<Unit> = withContext(ioDispatcher) {
        // The "an alarm must say something" rule, checked **before the DAO is touched at all** so
        // that a refusal can never half-write a row.
        //
        // The refusal is **tagged**, not just worded. `message` is developer-facing and a screen
        // that told a refusal from a write failure by reading it would break the moment somebody
        // rephrased this line; [BlankAlarmMessageException] is the typed channel, and it is handed
        // over as a value rather than thrown. The difference matters above: retrying a refusal can
        // never succeed, so a screen that offers "please try again" for one is lying.
        if (alarm.message.isBlank()) {
            Log.w(TAG, "save refused: the alarm has a blank message")
            return@withContext Outcome.Error(
                message = "An alarm cannot be saved with a blank message.",
                throwable = BlankAlarmMessageException(),
            )
        }

        val stamped = alarm.copy(
            // An alarm that has never been stored gets its stamp now. One that has been stored
            // before keeps the `createdAt` it already has — clobbering it here is the exact bug
            // that makes every alarm in the app report having been created the moment it was last
            // edited, and nothing on a screen would show it.
            createdAt = if (alarm.createdAt == Alarm.UNSAVED_AT) clock.millis() else alarm.createdAt,
        )

        try {
            alarmDao.upsert(alarm = stamped.toEntity())
            Outcome.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "save(id=${alarm.id}) failed", e)
            Outcome.Error(message = "The alarm could not be saved.", throwable = e)
        }
    }

    companion object {

        private const val TAG = "AlarmRepositoryImpl"

        /*
         * --- Why the sort lives here and not in the SQL (simple story) ---
         *
         * `AlarmDao.observeAll()` has no `ORDER BY` at all, and this comparator is the only thing
         * deciding what order an alarm list comes out in.
         *
         * "Earliest time of day first" is a promise this store makes to everything above it, and a
         * promise that lived only inside a SQL string would be one a future query could drop
         * without anything noticing — no compiler error, no failing test, just a list that is
         * subtly in the wrong order. Nothing in this repository can run SQLite, either: there is no
         * emulator and no Robolectric, so a SQL `ORDER BY` is literally uncheckable here. Sorted in
         * Kotlin, the promise belongs to the repository, and an ordinary JVM test can hold it to it
         * with a fake DAO that hands back a deliberately jumbled list.
         *
         * The `id` tiebreaker is not decoration: two alarms set for the same minute would otherwise
         * be free to swap places between emissions, and a list that reshuffles itself while the
         * user is looking at it is the kind of bug that reproduces once a week.
         */
        private val EARLIEST_FIRST: Comparator<Alarm> =
            compareBy(Alarm::hourOfDay).thenBy(Alarm::minute).thenBy(Alarm::id)
    }
}
