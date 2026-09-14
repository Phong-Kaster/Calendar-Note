package com.example.skeleton.ui.fragment.alarm_editor

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.enums.AlarmRepeatMode
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.model.BlankAlarmMessageException
import com.example.skeleton.domain.repository.AlarmRepository
import java.time.DayOfWeek
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Checks what the alarm editor does with the alarm it was opened for: that it carries the stored
 * alarm's id and creation time back into the store untouched, that it tells a refused save apart
 * from a failed one, and that it writes nothing at all when there was never an alarm behind it.
 *
 * `Dispatchers.setMain` is what makes any of this possible: `viewModelScope` posts to
 * `Dispatchers.Main`, which does not exist off a device. Replaced with an unconfined test
 * dispatcher, every `launch` in the ViewModel runs to completion the moment it is started, so a test
 * can call a function and assert on `uiState.value` on the very next line.
 *
 * That same convenience is why [FakeAlarmRepository] can be *held open* with a gate: without one,
 * there is no instant at which a read is "in flight", and `isLoading` — the flag that stops the time
 * control being composed with the wrong numbers — could not be observed at all.
 *
 * What is **not** covered here, and deliberately: the blank-message rule itself. It belongs to the
 * store, where every caller reaches it, and `AlarmRepositoryImplTest` holds it to it. The test below
 * about a blank message is the opposite assertion — that this screen does **not** check it a second
 * time, because two copies of one rule is one copy that gets missed.
 *
 * @author Phong-Kaster
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmEditorViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun replaceMainDispatcher() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun restoreMainDispatcher() {
        Dispatchers.resetMain()
    }

    // ---------- Opening ----------

    @Test
    fun `a new alarm opens on the default time with nothing written on it`() = runTest {
        val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository())

        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

        assertEquals("", viewModel.uiState.value.message)
        assertEquals(Alarm.DEFAULT_HOUR_OF_DAY, viewModel.uiState.value.hourOfDay)
        assertEquals(Alarm.DEFAULT_MINUTE, viewModel.uiState.value.minute)
    }

    @Test
    fun `a new alarm never shows the spinner, because there is nothing to read`() = runTest {
        // Not a cosmetic point. `CoreLayout(showLoading = …)` draws a spinner *instead of* the
        // content, so an editor that raised the flag for a brand-new alarm and never lowered it
        // would be a screen the user could not type on.
        val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository())

        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `an existing alarm opens with its own message and time`() = runTest {
        val repository = FakeAlarmRepository(stored = A_STORED_ALARM)
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)

        viewModel.openAlarm(alarmId = 7L)

        assertEquals("Take the bread out of the freezer", viewModel.uiState.value.message)
        assertEquals(21, viewModel.uiState.value.hourOfDay)
        assertEquals(30, viewModel.uiState.value.minute)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `the editor is not drawn until the alarm it was opened for has arrived`() = runTest {
        // **The flag this asserts is load-bearing, not decoration.** Material's `TimeInput` takes
        // its hour and minute at the moment it is first composed and never looks at them again, so
        // an editor composed before the read returned would show 08:00 for an alarm set to 21:30 —
        // and 08:00 is then what a save would store. The spinner is what guarantees the editor's
        // first composition is the one that already has the real numbers.
        //
        // The gate is the only way to stand inside the read: with an unconfined dispatcher the
        // coroutine would otherwise finish before the next line of this test ran, and `isLoading`
        // would be false at every instant a test could look at it.
        val gate = CompletableDeferred<Unit>()
        val repository = FakeAlarmRepository(stored = A_STORED_ALARM, readGate = gate)
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)

        viewModel.openAlarm(alarmId = 7L)
        assertTrue(viewModel.uiState.value.isLoading)

        gate.complete(Unit)

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals(21, viewModel.uiState.value.hourOfDay)
        assertEquals(30, viewModel.uiState.value.minute)
    }

    @Test
    fun `opening the same alarm twice does not throw away what the user typed`() = runTest {
        // This is the rotation case, and it is the normal course of events rather than an edge: the
        // Fragment is rebuilt and calls `openAlarm` from `onCreate` again, while the ViewModel — and
        // the half-written alarm in it — survives.
        val repository = FakeAlarmRepository(stored = A_STORED_ALARM)
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = 7L)
        viewModel.setMessage(value = "Take the bread out earlier")
        viewModel.setTime(hourOfDay = 6, minute = 45)

        viewModel.openAlarm(alarmId = 7L)

        assertEquals("Take the bread out earlier", viewModel.uiState.value.message)
        assertEquals(6, viewModel.uiState.value.hourOfDay)
        assertEquals(45, viewModel.uiState.value.minute)
    }

    // ---------- Opening an alarm that is not there ----------

    @Test
    fun `an alarm that has since been removed does not open as a fresh one, and cannot be saved over`() =
        runTest {
            // The defect this shape exists to prevent, learned on the note editor: falling through
            // to a blank draft let the user retype the alarm they thought they were editing, and
            // Room's `autoGenerate` filed it as a *second* row. The list then showed two alarms, and
            // nothing anywhere said so.
            val repository = FakeAlarmRepository(stored = null)
            val viewModel = AlarmEditorViewModel(alarmRepository = repository)

            viewModel.openAlarm(alarmId = 7L)

            assertTrue(viewModel.uiState.value.openFailed)
            assertFalse(viewModel.uiState.value.isLoading)

            viewModel.setMessage(value = "Take the bread out of the freezer")
            viewModel.save()

            // The assertion that matters. A save from here is the duplicate.
            assertEquals(0, repository.saveCount)
            assertNull(repository.savedAlarm)
        }

    @Test
    fun `an alarm the store cannot read reports the failure instead of opening blank`() = runTest {
        // Told apart from the case above at the store — "it is gone" is a fact, "I could not look"
        // is a failure — even though this screen currently answers both the same way. It has to
        // reach the same refusal, because answering an unreadable store with an empty editor is the
        // screen inventing an answer it does not have, and the alarm it would duplicate may well
        // still exist.
        val repository = FakeAlarmRepository(stored = null, readFails = true)
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)

        viewModel.openAlarm(alarmId = 7L)

        assertTrue(viewModel.uiState.value.openFailed)

        viewModel.save()

        assertEquals(0, repository.saveCount)
    }

    @Test
    fun `the open failure clears once it has been shown`() = runTest {
        val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository(stored = null))
        viewModel.openAlarm(alarmId = 7L)
        // Asserted *before* consuming, or this test proves nothing: the field starts false, so an
        // `openAlarm` that stopped reporting problems at all would leave the assertion below
        // passing.
        assertTrue(viewModel.uiState.value.openFailed)

        viewModel.consumeOpenFailed()

        assertFalse(viewModel.uiState.value.openFailed)
    }

    @Test
    fun `nothing is written before an alarm has been opened`() = runTest {
        // The ViewModel exists before `openAlarm` runs — the Fragment constructs it and then calls
        // in from `onCreate`. Until it does there is no alarm here, and `save` has to know that
        // rather than acting on a plausible-looking blank one.
        val repository = FakeAlarmRepository()
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)

        viewModel.setMessage(value = "Take the bread out of the freezer")
        viewModel.save()

        assertEquals(0, repository.saveCount)
    }

    // ---------- Saving ----------

    @Test
    fun `saving a new alarm hands the store what was written, unstamped`() = runTest {
        val repository = FakeAlarmRepository()
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

        viewModel.setMessage(value = "Leave for the dentist")
        viewModel.setTime(hourOfDay = 14, minute = 5)
        viewModel.save()

        val saved = repository.savedAlarm!!
        assertEquals("Leave for the dentist", saved.message)
        assertEquals(14, saved.hourOfDay)
        assertEquals(5, saved.minute)
        // Handed over unstamped and without an id: the store, and nothing else in this app, decides
        // what time it is, and Room decides what the row id is.
        assertEquals(Alarm.UNSAVED_ID, saved.id)
        assertEquals(Alarm.UNSAVED_AT, saved.createdAt)
    }

    @Test
    fun `saving an edited alarm keeps its id and its original creation time`() = runTest {
        // Lose the id and the next save writes a second alarm instead of changing the first. Lose
        // the `createdAt` and the store, which only replaces a zero, stamps it as brand new.
        val repository = FakeAlarmRepository(stored = A_STORED_ALARM)
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = 7L)

        viewModel.setMessage(value = "Take the bread out earlier")
        viewModel.setTime(hourOfDay = 6, minute = 45)
        viewModel.save()

        val saved = repository.savedAlarm!!
        assertEquals(7L, saved.id)
        assertEquals(1_000L, saved.createdAt)
        assertEquals("Take the bread out earlier", saved.message)
        assertEquals(6, saved.hourOfDay)
        assertEquals(45, saved.minute)
        // Carried through untouched even though no screen draws it. The column exists, so a field
        // silently reset to its default on every edit would disarm alarms the user had switched off
        // — or leave them armed — the moment the task that adds the switch lands.
        assertTrue(saved.enabled)
    }

    @Test
    fun `editing an alarm that was switched off does not switch it back on`() = runTest {
        // The Alarms list can now switch an alarm off, and this screen does not draw that flag at
        // all — so the only thing standing between "off" and "on again the next time somebody fixes
        // a typo" is the alarm being carried through whole. A ViewModel that rebuilt the alarm from
        // the three fields it *does* draw would re-arm every alarm the user had silenced, and
        // nothing on either screen would look wrong until one went off at six in the morning.
        val repository = FakeAlarmRepository(stored = A_SWITCHED_OFF_ALARM)
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = 8L)

        viewModel.setMessage(value = "Leave for the dentist, early")
        viewModel.save()

        val saved = repository.savedAlarm!!
        assertFalse(saved.enabled)
        assertEquals(8L, saved.id)
        assertEquals("Leave for the dentist, early", saved.message)
    }

    @Test
    fun `the editor never deletes anything`() = runTest {
        // Deleting belongs to the Alarms list, and this is the assertion that says the editor has
        // not grown a second path to it. Opening, editing and saving an alarm must leave the store's
        // delete untouched.
        val repository = FakeAlarmRepository(stored = A_STORED_ALARM)
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = 7L)

        viewModel.setMessage(value = "Take the bread out earlier")
        viewModel.save()

        assertEquals(0, repository.deleteCount)
    }

    @Test
    fun `a blank message still reaches the store, which owns the rule`() = runTest {
        // Deliberately the opposite of a guard. Checking "is it blank?" here as well would be a
        // second copy of a rule that lives in the store, and two copies is one that gets missed —
        // the screen would start refusing alarms the store would have accepted, or the other way
        // round, the first time either side changed.
        val repository = FakeAlarmRepository()
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

        viewModel.save()

        assertEquals(1, repository.saveCount)
        assertEquals("", repository.savedAlarm!!.message)
    }

    @Test
    fun `a successful save raises the trigger the screen leaves on`() = runTest {
        val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository())
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        assertEquals(0, viewModel.uiState.value.savedTrigger)

        viewModel.setMessage(value = "Leave for the dentist")
        viewModel.save()

        assertEquals(1, viewModel.uiState.value.savedTrigger)
        assertFalse(viewModel.uiState.value.saveFailed)
        assertFalse(viewModel.uiState.value.saveRefusedBlank)
    }

    @Test
    fun `tapping save twice writes the alarm once`() = runTest {
        // The screen leaves on the first success, but leaving is animated and the composition stays
        // alive and taking touches while it plays. A second tap in that window reaches the store
        // with the alarm still carrying `UNSAVED_ID` — this ViewModel never learns the id the store
        // handed out — so Room's `autoGenerate` would file a second row. One intent, two identical
        // alarms, and nothing to tell them apart.
        val repository = FakeAlarmRepository()
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        viewModel.setMessage(value = "Leave for the dentist")

        viewModel.save()
        viewModel.save()

        assertEquals(1, repository.saveCount)
        assertEquals(1, viewModel.uiState.value.savedTrigger)
    }

    // ---------- A save that did not happen: failure and refusal are different events ----------

    @Test
    fun `a save that failed reports the failure and does not leave the screen`() = runTest {
        // The screen must stay where it is. Navigating back on a failure would drop what the user
        // wrote and tell them nothing about why.
        //
        // A plain error, with no tag on it: this is the disk-went-wrong path, and the assertion that
        // it is **not** reported as a refusal is what keeps the two apart. Tagging every error as a
        // refusal would pass the refusal test below and would tell a user whose database is failing
        // that they forgot to write anything.
        val repository = FakeAlarmRepository(outcome = Outcome.Error(message = "the disk is gone"))
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        viewModel.setMessage(value = "Leave for the dentist")

        viewModel.save()

        assertEquals(0, viewModel.uiState.value.savedTrigger)
        assertTrue(viewModel.uiState.value.saveFailed)
        assertFalse(viewModel.uiState.value.saveRefusedBlank)
    }

    @Test
    fun `a refused save is reported as a refusal and not as a failure`() = runTest {
        // The whole point of telling the two apart, and the reason the store tags its refusal by
        // **type** rather than by wording. The screen says "please try again" for a failure, and a
        // retry of a *refusal* can never succeed — the message is as blank as it was a moment ago.
        // So the refusal has to arrive as its own event, and it must not raise `saveFailed`: the
        // message for that one is an instruction the user cannot act on.
        val repository = FakeAlarmRepository(
            outcome = Outcome.Error(
                message = "An alarm cannot be saved with a blank message.",
                throwable = BlankAlarmMessageException(),
            ),
        )
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

        viewModel.save()

        assertTrue(viewModel.uiState.value.saveRefusedBlank)
        assertFalse(viewModel.uiState.value.saveFailed)
        // And the user is still on the screen with their time, exactly as after a failure.
        assertEquals(0, viewModel.uiState.value.savedTrigger)
    }

    @Test
    fun `the refusal clears once it has been shown`() = runTest {
        val repository = FakeAlarmRepository(
            outcome = Outcome.Error(message = "refused", throwable = BlankAlarmMessageException()),
        )
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        viewModel.save()
        // Asserted *before* consuming, or this test proves nothing: the field starts false, so a
        // `save` that never raised it would leave the assertion below passing.
        assertTrue(viewModel.uiState.value.saveRefusedBlank)

        viewModel.consumeSaveRefused()

        assertFalse(viewModel.uiState.value.saveRefusedBlank)
    }

    @Test
    fun `the failure clears once it has been shown`() = runTest {
        val repository = FakeAlarmRepository(outcome = Outcome.Error(message = "the disk is gone"))
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        viewModel.setMessage(value = "Leave for the dentist")
        viewModel.save()
        assertTrue(viewModel.uiState.value.saveFailed)

        viewModel.consumeSaveFailed()

        assertFalse(viewModel.uiState.value.saveFailed)
    }

    @Test
    fun `save works again after a refusal`() = runTest {
        // A refusal leaves the user on the screen, so the button has to work again — otherwise
        // somebody who tapped Save one word too early is stranded with an alarm they can never
        // store, on a screen whose only other exit throws it away.
        val repository = FakeAlarmRepository(
            outcome = Outcome.Error(message = "refused", throwable = BlankAlarmMessageException()),
        )
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

        viewModel.save()
        viewModel.save()

        assertEquals(2, repository.saveCount)
    }

    @Test
    fun `save works again after a failure`() = runTest {
        // The other half of the guard above: a failed write leaves the user sitting on the screen
        // with their text, so a guard that stayed shut would leave them with no way to keep it.
        val repository = FakeAlarmRepository(outcome = Outcome.Error(message = "the disk is gone"))
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        viewModel.setMessage(value = "Leave for the dentist")

        viewModel.save()
        viewModel.save()

        assertEquals(2, repository.saveCount)
    }

    // ---------- Repeat ----------

    @Test
    fun `a new alarm opens on the daily repeat, matching what every alarm did before this field existed`() =
        runTest {
            val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository())

            viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

            assertEquals(AlarmRepeatMode.DAILY, viewModel.uiState.value.repeatMode)
            assertEquals(emptySet<DayOfWeek>(), viewModel.uiState.value.repeatDays)
        }

    @Test
    fun `opening a stored alarm shows the repeat option and weekdays it was saved with`() = runTest {
        val repository = FakeAlarmRepository(stored = A_CUSTOM_REPEAT_ALARM)
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)

        viewModel.openAlarm(alarmId = 9L)

        assertEquals(AlarmRepeatMode.CUSTOM, viewModel.uiState.value.repeatMode)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), viewModel.uiState.value.repeatDays)
    }

    @Test
    fun `picking a repeat option mirrors it into the screen state`() = runTest {
        val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository())
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

        viewModel.setRepeatMode(repeatMode = AlarmRepeatMode.ONE_TIME)

        assertEquals(AlarmRepeatMode.ONE_TIME, viewModel.uiState.value.repeatMode)
    }

    @Test
    fun `switching away from custom does not clear the weekdays already ticked`() = runTest {
        // Alarm.repeatDays is carried regardless of repeatMode on purpose — switching back to
        // Custom later must show the same ticks, not an editor that has forgotten them.
        val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository())
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        viewModel.setRepeatMode(repeatMode = AlarmRepeatMode.CUSTOM)
        viewModel.toggleRepeatDay(day = DayOfWeek.TUESDAY)

        viewModel.setRepeatMode(repeatMode = AlarmRepeatMode.DAILY)

        assertEquals(setOf(DayOfWeek.TUESDAY), viewModel.uiState.value.repeatDays)
    }

    @Test
    fun `tapping a weekday twice ticks it and then unticks it`() = runTest {
        val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository())
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)

        viewModel.toggleRepeatDay(day = DayOfWeek.FRIDAY)
        assertEquals(setOf(DayOfWeek.FRIDAY), viewModel.uiState.value.repeatDays)

        viewModel.toggleRepeatDay(day = DayOfWeek.FRIDAY)
        assertEquals(emptySet<DayOfWeek>(), viewModel.uiState.value.repeatDays)
    }

    @Test
    fun `toggling one weekday leaves the others exactly as they were`() = runTest {
        val viewModel = AlarmEditorViewModel(alarmRepository = FakeAlarmRepository())
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        viewModel.toggleRepeatDay(day = DayOfWeek.MONDAY)
        viewModel.toggleRepeatDay(day = DayOfWeek.WEDNESDAY)

        viewModel.toggleRepeatDay(day = DayOfWeek.FRIDAY)

        assertEquals(
            setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            viewModel.uiState.value.repeatDays,
        )
    }

    @Test
    fun `saving hands the store the repeat option and weekdays currently on screen`() = runTest {
        val repository = FakeAlarmRepository()
        val viewModel = AlarmEditorViewModel(alarmRepository = repository)
        viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
        viewModel.setMessage(value = "Take the bins out")

        viewModel.setRepeatMode(repeatMode = AlarmRepeatMode.CUSTOM)
        viewModel.toggleRepeatDay(day = DayOfWeek.MONDAY)
        viewModel.toggleRepeatDay(day = DayOfWeek.THURSDAY)
        viewModel.save()

        val saved = repository.savedAlarm!!
        assertEquals(AlarmRepeatMode.CUSTOM, saved.repeatMode)
        assertEquals(setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY), saved.repeatDays)
    }

    @Test
    fun `saving a one-time alarm carries no weekdays even if some were ticked before switching`() =
        runTest {
            val repository = FakeAlarmRepository()
            val viewModel = AlarmEditorViewModel(alarmRepository = repository)
            viewModel.openAlarm(alarmId = Alarm.UNSAVED_ID)
            viewModel.setMessage(value = "Catch the early train")
            viewModel.setRepeatMode(repeatMode = AlarmRepeatMode.CUSTOM)
            viewModel.toggleRepeatDay(day = DayOfWeek.TUESDAY)

            viewModel.setRepeatMode(repeatMode = AlarmRepeatMode.ONE_TIME)
            viewModel.save()

            val saved = repository.savedAlarm!!
            assertEquals(AlarmRepeatMode.ONE_TIME, saved.repeatMode)
            // Carried, not cleared — see the "does not clear" test above. `ONE_TIME` simply never
            // reads this set, the same way `AlarmRepeatMode.CUSTOM`'s own KDoc explains.
            assertEquals(setOf(DayOfWeek.TUESDAY), saved.repeatDays)
        }

    private companion object {

        /** An alarm that already exists in the store, with row id 7, set for half past nine. */
        private val A_STORED_ALARM = Alarm(
            id = 7L,
            message = "Take the bread out of the freezer",
            hourOfDay = 21,
            minute = 30,
            enabled = true,
            createdAt = 1_000L,
        )

        /**
         * A stored alarm the user has switched **off** on the Alarms list.
         *
         * This editor draws no switch, so the flag is invisible to it from start to finish — which
         * is exactly why an alarm in this state is worth a fixture of its own.
         */
        private val A_SWITCHED_OFF_ALARM = Alarm(
            id = 8L,
            message = "Leave for the dentist",
            hourOfDay = 14,
            minute = 5,
            enabled = false,
            createdAt = 2_000L,
        )

        /** A stored alarm saved with `CUSTOM` repeat, ticked for Monday and Thursday. */
        private val A_CUSTOM_REPEAT_ALARM = Alarm(
            id = 9L,
            message = "Take the bins out",
            hourOfDay = 7,
            minute = 0,
            enabled = true,
            repeatMode = AlarmRepeatMode.CUSTOM,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
            createdAt = 3_000L,
        )
    }
}

/**
 * An alarms store that remembers what it was asked to do and answers with whatever it was built
 * with.
 *
 * [saveCount] is as important as [savedAlarm]. Several tests above are about something **not**
 * happening — a save that must not write a duplicate, a save that must not happen at all when there
 * was no alarm behind the screen — and "the last alarm handed over is still null" is a weaker
 * statement than "the store was never called".
 *
 * @param stored the alarm [getAlarm] answers with, or null to behave like a store that has never
 *   heard of the id being asked for. Note the difference from [readFails]: this one is a store that
 *   answers "no such alarm", which is not a failure.
 * @param outcome what [save] reports back. Defaults to success. A plain [Outcome.Error] is a write
 *   that went wrong; one carrying a [BlankAlarmMessageException] is the store refusing an alarm with
 *   nothing written on it. The screen has to say different things about those two, so the fake has
 *   to be able to be both.
 * @param readFails true to make [getAlarm] report that it could not read at all — a different answer
 *   from [stored] being null.
 * @param readGate when given, [getAlarm] **waits** on it before answering. That is what lets a test
 *   stand inside the moment a real read is in flight, which is the only moment
 *   [AlarmEditorUiState.isLoading] is ever true. Left null, the store answers straight away.
 * @author Phong-Kaster
 */
private class FakeAlarmRepository(
    private val stored: Alarm? = null,
    private val outcome: Outcome<Unit> = Outcome.Success(Unit),
    private val readFails: Boolean = false,
    private val readGate: CompletableDeferred<Unit>? = null,
) : AlarmRepository {

    /** The last alarm [save] was given, exactly as it was given. */
    var savedAlarm: Alarm? = null
        private set

    /** How many times [save] was called. An alarm written twice is one the user now has two of. */
    var saveCount: Int = 0
        private set

    /** How many times [delete] was called. It should be zero for every test in this file. */
    var deleteCount: Int = 0
        private set

    // The editor never reads the whole list — it opens one alarm by id — so this is here to satisfy
    // the interface and nothing more.
    override val alarmsFlow: Flow<List<Alarm>> = flowOf(listOfNotNull(stored))

    override suspend fun getAlarm(id: Long): Outcome<Alarm?> {
        readGate?.await()
        if (readFails) return Outcome.Error(message = "the store would not answer")
        return Outcome.Success(stored?.takeIf { alarm -> alarm.id == id })
    }

    override suspend fun save(alarm: Alarm): Outcome<Unit> {
        savedAlarm = alarm
        saveCount++
        return outcome
    }

    /**
     * Counted rather than implemented, because **this screen must never delete anything**.
     *
     * Removing an alarm belongs to the Alarms list, where the bin sits next to the alarm and the
     * confirmation names it. A delete reaching the store from the editor would be a second path to
     * the one irreversible thing this app does, and the assertion that [deleteCount] stays zero is
     * what says the editor has not quietly grown one.
     */
    override suspend fun delete(alarm: Alarm): Outcome<Unit> {
        deleteCount++
        return Outcome.Success(Unit)
    }
}
