package com.example.skeleton.data.repository.impl

import android.util.Log
import com.example.skeleton.common.Outcome
import com.example.skeleton.data.database.local.dao.AlarmDao
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.model.BlankAlarmMessageException
import com.example.skeleton.domain.repository.AlarmRepository
import com.example.skeleton.domain.scheduler.AlarmScheduler
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
 * **It also keeps the phone's own list of pending alarms in step with the table.** That mirroring
 * lives here, and not on a screen, because this is the one place *every* write already funnels
 * through: the editor saving, a switch being flipped in the list, a delete being confirmed. A screen
 * that remembered to arm an alarm after saving it would be a screen that some other screen forgets
 * to copy — and the thing it forgets is invisible until an alarm the user deleted goes off anyway.
 *
 * The mirror is **best-effort and never changes the answer**. The database is the truth; a phone that
 * refuses to arm an alarm (the exact-alarm permission can be withdrawn mid-write) must not turn a row
 * that was written perfectly well into a failure on screen.
 *
 * @param alarmDao the table's accessor.
 * @param alarmScheduler the thing that makes an alarm actually go off, kept in step with every
 *   successful write. An interface and not `AlarmManager`, because the platform's alarm calls are
 *   invisible to a unit test here — a test hands over a fake that writes down what it was asked to
 *   do, and that is how "a deleted alarm is also cancelled" is checkable at all.
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
    private val alarmScheduler: AlarmScheduler,
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
            // The row id Room hands back, not `stamped.id`. For an edit the two are the same, but a
            // brand-new alarm arrives carrying [Alarm.UNSAVED_ID] — `0` — and the scheduler refuses
            // to arm an alarm with no id (it would be filed under an address every unsaved alarm
            // shares). Arming `stamped` itself would therefore mean that **no alarm the user ever
            // creates goes off**, while the row landed in the table and the list looked perfect.
            val rowId = alarmDao.upsert(alarm = stamped.toEntity())
            mirrorSchedule(alarm = stamped.copy(id = rowId))
            Outcome.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "save(id=${alarm.id}) failed", e)
            Outcome.Error(message = "The alarm could not be saved.", throwable = e)
        }
    }

    override suspend fun delete(alarm: Alarm): Outcome<Unit> = withContext(ioDispatcher) {
        // An alarm that was never stored has no row to remove, and Room's `@Delete` would match
        // nothing and report success. The screen above would then tell the user their alarm was
        // deleted while it sat in the list untouched — so the refusal happens here, where every
        // caller reaches it, exactly like the blank-message rule above.
        if (alarm.id == Alarm.UNSAVED_ID) {
            Log.w(TAG, "delete refused: the alarm has never been stored")
            return@withContext Outcome.Error(
                message = "An alarm that was never saved cannot be deleted.",
            )
        }

        try {
            // The count is the whole point of asking. Room's `@Delete` matches on the primary key
            // and is perfectly content to match nothing — so an alarm removed a moment ago, or a
            // stale id from a list that has not caught up, would come back here as a success and
            // the screen would say "Alarm deleted" about a row that was already gone. The sentinel
            // check above catches only *one* way of having no row; this catches all of them.
            val removed = alarmDao.delete(alarm = alarm.toEntity())
            if (removed == 0) {
                Log.w(TAG, "delete(id=${alarm.id}) matched no row")
                return@withContext Outcome.Error(message = "There was no such alarm to delete.")
            }

            // Only once a row really went. **This is the half that must never be skipped.** A row
            // removed from the table while its alarm stays armed still goes off, still shows its
            // notification, and — because the receiver re-arms itself afterwards — goes off again
            // every day, for an alarm the user can no longer see and therefore cannot delete twice.
            mirrorCancel(alarmId = alarm.id)

            Outcome.Success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "delete(id=${alarm.id}) failed", e)
            Outcome.Error(message = "The alarm could not be deleted.", throwable = e)
        }
    }

    /**
     * Makes the phone's pending alarms agree with the alarm that was just written.
     *
     * **Cancel first, always, then arm only if it is switched on.** Both halves are deliberate:
     *
     * - Cancelling *unconditionally* is what makes an edit safe. Moving an alarm from 07:00 to 08:00
     *   arms a new one, and without cancelling the old one the user now has two — one of them for a
     *   time that is no longer written anywhere. `AlarmScheduler.cancel` on an id nothing was armed
     *   for is a documented no-op, so this is harmless for a brand-new alarm too.
     * - Arming only when `enabled` is what makes the switch mean something. A switched-off alarm ends
     *   this function with nothing pending, which is the whole point of flipping the switch.
     *
     * @param alarm the alarm exactly as it now sits in the table, **carrying its real row id**.
     */
    private fun mirrorSchedule(alarm: Alarm) {
        // The write already succeeded. Whatever happens in here, it is not allowed to change that
        // answer: the database is the truth and the schedule is a best-effort copy of it, so a phone
        // that has just withdrawn the exact-alarm permission must not make a good save read as a
        // failure on screen. `CancellationException` is not re-thrown here — unlike the DAO calls
        // above, these are plain non-suspending calls and cancellation cannot arrive through them.
        try {
            alarmScheduler.cancel(alarmId = alarm.id)

            if (alarm.enabled) alarmScheduler.schedule(alarm = alarm)
        } catch (e: Exception) {
            Log.w(TAG, "alarm ${alarm.id} was saved, but its schedule was not updated", e)
        }
    }

    /**
     * Takes an alarm off the phone after its row has gone, defended the same way as [mirrorSchedule].
     *
     * @param alarmId the row id of the alarm that was just removed.
     */
    private fun mirrorCancel(alarmId: Long) {
        try {
            alarmScheduler.cancel(alarmId = alarmId)
        } catch (e: Exception) {
            Log.w(TAG, "alarm $alarmId was deleted, but its schedule was not cancelled", e)
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
