package com.example.skeleton.ui.fragment.alarms

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.repository.AlarmRepository
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
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
 * Checks the two things the Alarms screen can do to an alarm without opening it — switch it off and
 * remove it — and, above all, **that removing one takes two steps**.
 *
 * That last point is the reason this file exists. "Deleting asks first" is the kind of promise that
 * is usually only a habit of a layout: a sheet is opened, and the delete happens to live inside it.
 * Here it is a fact about [AlarmsViewModel] instead — `askToDelete` writes a field and touches no
 * store, and `confirmDelete` refuses to do anything unless that field is set — and a fact is
 * something a plain JVM test can hold the class to. A picture of the sheet never could: it cannot
 * show what a tap reaches.
 *
 * `Dispatchers.setMain` is what makes any of this possible: `viewModelScope` posts to
 * `Dispatchers.Main`, which does not exist off a device. Replaced with an unconfined test
 * dispatcher, every `launch` in the ViewModel runs to completion the moment it is started, so a test
 * can call a function and assert on `uiState.value` on the very next line.
 *
 * That same convenience is why [FakeAlarmRepository] can be *held open* with a gate: without one,
 * there is no instant at which a delete is "in flight", and the double-tap guard could not be
 * observed at all.
 *
 * What is **not** covered here, and deliberately: the ordering of the list, the blank-message rule
 * and "a delete that matched no row is an error". All three belong to the store, where every caller
 * reaches them, and `AlarmRepositoryImplTest` holds it to them. This screen only has to pass a
 * refusal on to the user without pretending it was a success.
 *
 * @author Phong-Kaster
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlarmsViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun replaceMainDispatcher() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun restoreMainDispatcher() {
        Dispatchers.resetMain()
    }

    // ---------- The list ----------

    @Test
    fun `the screen shows what the store holds, in the order the store handed it over`() = runTest {
        // Ordering is the store's promise and this screen must not have a second opinion about it.
        // The fake hands these over in an order no comparator here would produce, and the assertion
        // is that the ViewModel passed them straight through.
        val repository = FakeAlarmRepository(alarms = listOf(ANOTHER_ALARM, AN_ALARM))

        val viewModel = AlarmsViewModel(alarmRepository = repository)

        assertEquals(listOf(2L, 1L), viewModel.uiState.value.alarms.map { alarm -> alarm.id })
        assertFalse(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `an empty store is an empty screen, not a blank one`() = runTest {
        val viewModel = AlarmsViewModel(alarmRepository = FakeAlarmRepository(alarms = emptyList()))

        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `the list keeps up with the store while the screen is open`() = runTest {
        // The reason this ViewModel subscribes instead of reading once. A one-shot read in `init`
        // would show a list frozen at first construction, and the alarm the user just wrote on the
        // editor would be missing when they came back.
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        repository.emit(alarms = listOf(AN_ALARM, ANOTHER_ALARM))

        assertEquals(listOf(1L, 2L), viewModel.uiState.value.alarms.map { alarm -> alarm.id })
    }

    // ---------- Asking to delete is not deleting ----------

    @Test
    fun `asking to delete opens the confirmation and removes nothing`() = runTest {
        // **The assertion this whole file is built around.** One tap on the bin must reach the
        // question and nothing else; `deleteCount` is what says so, because "the alarm is still in
        // the list" would also be true of a delete that failed.
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        viewModel.askToDelete(alarmId = 1L)

        assertEquals(1L, viewModel.uiState.value.pendingDeleteId)
        assertTrue(viewModel.uiState.value.confirmingDelete)
        assertEquals(0, repository.deleteCount)
        assertEquals(0, viewModel.uiState.value.deletedTrigger)
    }

    @Test
    fun `a delete that skipped the confirmation does nothing at all`() = runTest {
        // The sheet is the only caller today, and "the only caller today" is not a guarantee. With
        // the guard, a future screen that wired a delete straight to a row loses nothing; without
        // it, the loss would be silent and permanent.
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        viewModel.confirmDelete()

        assertEquals(0, repository.deleteCount)
        assertEquals(1, viewModel.uiState.value.alarms.size)
        assertEquals(0, viewModel.uiState.value.deletedTrigger)
    }

    @Test
    fun `backing out of the confirmation leaves the alarm alone`() = runTest {
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)
        viewModel.askToDelete(alarmId = 1L)

        viewModel.dismissDelete()
        // Confirming *after* backing out has to be refused as well. The sheet is gone from the
        // screen, but a stray call must not find a question still standing open behind it.
        viewModel.confirmDelete()

        assertNull(viewModel.uiState.value.pendingDeleteId)
        assertEquals(0, repository.deleteCount)
    }

    // ---------- Confirming ----------

    @Test
    fun `confirming removes the alarm that was asked about, and only that one`() = runTest {
        // The middle of three on purpose. A ViewModel that deleted "the first one" or "the last one"
        // would pass a one-alarm test perfectly, and the user would lose the wrong alarm — the one
        // irreversible mistake this screen can make.
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM, ANOTHER_ALARM, A_THIRD_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)
        viewModel.askToDelete(alarmId = 2L)

        viewModel.confirmDelete()

        assertEquals(2L, repository.deletedAlarm?.id)
        assertEquals(listOf(1L, 3L), viewModel.uiState.value.alarms.map { alarm -> alarm.id })
    }

    @Test
    fun `a successful delete closes the confirmation and says so once`() = runTest {
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)
        viewModel.askToDelete(alarmId = 1L)

        viewModel.confirmDelete()

        assertNull(viewModel.uiState.value.pendingDeleteId)
        assertEquals(1, viewModel.uiState.value.deletedTrigger)
        assertFalse(viewModel.uiState.value.deleteFailed)
    }

    @Test
    fun `confirming twice in the same instant removes the alarm once`() = runTest {
        // The gate is the only way to stand inside the delete: with an unconfined dispatcher the
        // coroutine would otherwise finish before the next line of this test ran, and the second tap
        // would be refused by the confirmation having already closed rather than by the guard that
        // is on trial here.
        //
        // Without the guard the second tap reaches a row that is already gone, the store reports
        // "there was no such alarm", and the user is told their deletion failed one breath after
        // being told it worked.
        val gate = CompletableDeferred<Unit>()
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM), deleteGate = gate)
        val viewModel = AlarmsViewModel(alarmRepository = repository)
        viewModel.askToDelete(alarmId = 1L)

        viewModel.confirmDelete()
        viewModel.confirmDelete()
        gate.complete(Unit)

        assertEquals(1, repository.deleteCount)
        assertEquals(1, viewModel.uiState.value.deletedTrigger)
        assertFalse(viewModel.uiState.value.deleteFailed)
    }

    @Test
    fun `a delete the store refused is reported, and the confirmation closes`() = runTest {
        // The sheet has to come down even on a failure: left standing under an error message it
        // reads as though tapping the same button again might work, and here it never will.
        val repository = FakeAlarmRepository(
            alarms = listOf(AN_ALARM),
            deleteOutcome = Outcome.Error(message = "there was no such alarm to delete"),
        )
        val viewModel = AlarmsViewModel(alarmRepository = repository)
        viewModel.askToDelete(alarmId = 1L)

        viewModel.confirmDelete()

        assertTrue(viewModel.uiState.value.deleteFailed)
        assertEquals(0, viewModel.uiState.value.deletedTrigger)
        assertNull(viewModel.uiState.value.pendingDeleteId)
        // And the alarm is still there, which is the point of reporting it at all.
        assertEquals(1, viewModel.uiState.value.alarms.size)
    }

    @Test
    fun `confirming an alarm that has already left the list deletes nothing else`() = runTest {
        // The race: the row went while the question was on screen. The tempting shortcut — delete
        // whatever is at that position now — is how somebody loses an alarm they never chose. There
        // is nothing to delete, so nothing is asked of the store at all.
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)
        viewModel.askToDelete(alarmId = 404L)

        viewModel.confirmDelete()

        assertEquals(0, repository.deleteCount)
        assertTrue(viewModel.uiState.value.deleteFailed)
        assertEquals(1, viewModel.uiState.value.alarms.size)
    }

    @Test
    fun `the delete failure clears once it has been shown`() = runTest {
        val repository = FakeAlarmRepository(
            alarms = listOf(AN_ALARM),
            deleteOutcome = Outcome.Error(message = "the disk is gone"),
        )
        val viewModel = AlarmsViewModel(alarmRepository = repository)
        viewModel.askToDelete(alarmId = 1L)
        viewModel.confirmDelete()
        // Asserted *before* consuming, or this test proves nothing: the field starts false, so a
        // delete that stopped reporting problems at all would leave the assertion below passing.
        assertTrue(viewModel.uiState.value.deleteFailed)

        viewModel.consumeDeleteFailed()

        assertFalse(viewModel.uiState.value.deleteFailed)
    }

    @Test
    fun `deleting one alarm and then another both work`() = runTest {
        // The Alarms screen stays put after a delete, unlike the note editor — so the guard that
        // stops a double tap must open again, or the second alarm the user tries to remove is one
        // they can never remove.
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM, ANOTHER_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        viewModel.askToDelete(alarmId = 1L)
        viewModel.confirmDelete()
        viewModel.askToDelete(alarmId = 2L)
        viewModel.confirmDelete()

        assertEquals(2, repository.deleteCount)
        assertEquals(2, viewModel.uiState.value.deletedTrigger)
        assertTrue(viewModel.uiState.value.isEmpty)
    }

    @Test
    fun `the delete trigger clears once it has been shown, so a later delete is still announced`() =
        runTest {
            // The bug this guards against: the Alarms screen never leaves after a delete, unlike the
            // note editor. A `LaunchedEffect` keyed on this trigger fires the instant its composition
            // is entered, not only when the value changes from what that composition last saw — so a
            // trigger left sitting above zero would replay "Alarm deleted" on the next unrelated
            // recomposition. Consuming back to zero is what keeps a *second* delete visible instead of
            // looking like a no-op change from 1 to 1.
            val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM, ANOTHER_ALARM))
            val viewModel = AlarmsViewModel(alarmRepository = repository)
            viewModel.askToDelete(alarmId = 1L)
            viewModel.confirmDelete()
            assertEquals(1, viewModel.uiState.value.deletedTrigger)

            viewModel.consumeDeleted()
            assertEquals(0, viewModel.uiState.value.deletedTrigger)

            viewModel.askToDelete(alarmId = 2L)
            viewModel.confirmDelete()

            assertEquals(1, viewModel.uiState.value.deletedTrigger)
        }

    // ---------- Switching an alarm off without deleting it ----------

    @Test
    fun `switching an alarm off writes it through the store with everything else untouched`() =
        runTest {
            // It goes through the ordinary save, so the alarm has to arrive whole: lose the id and
            // the next write files a second alarm, lose the `createdAt` and the store stamps it as
            // brand new, lose the message and the store refuses it outright.
            val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
            val viewModel = AlarmsViewModel(alarmRepository = repository)

            viewModel.setEnabled(alarm = AN_ALARM, enabled = false)

            val saved = repository.savedAlarm!!
            assertFalse(saved.enabled)
            assertEquals(1L, saved.id)
            assertEquals("Take the bread out of the freezer", saved.message)
            assertEquals(7, saved.hourOfDay)
            assertEquals(30, saved.minute)
            assertEquals(1_000L, saved.createdAt)
        }

    @Test
    fun `an alarm switched off is still on the screen`() = runTest {
        // Off is not gone. If switching an alarm off took it off the list, the user would have no
        // way back to it and no way to tell the two actions apart.
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        viewModel.setEnabled(alarm = AN_ALARM, enabled = false)

        assertEquals(1, viewModel.uiState.value.alarms.size)
        assertFalse(viewModel.uiState.value.alarms.single().enabled)
    }

    @Test
    fun `switching an alarm back on writes it back on`() = runTest {
        val repository = FakeAlarmRepository(alarms = listOf(A_SWITCHED_OFF_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        viewModel.setEnabled(alarm = A_SWITCHED_OFF_ALARM, enabled = true)

        assertTrue(repository.savedAlarm!!.enabled)
        assertTrue(viewModel.uiState.value.alarms.single().enabled)
    }

    @Test
    fun `a switch redrawn into the position it was already in writes nothing`() = runTest {
        // Compose hands a switch its state back on every recomposition. A write for each of those
        // would touch the row — and, once alarms are scheduled, reschedule it — for a user who did
        // nothing at all.
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        viewModel.setEnabled(alarm = AN_ALARM, enabled = true)

        assertEquals(0, repository.saveCount)
    }

    @Test
    fun `switching an alarm never deletes it`() = runTest {
        val repository = FakeAlarmRepository(alarms = listOf(AN_ALARM))
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        viewModel.setEnabled(alarm = AN_ALARM, enabled = false)

        assertEquals(0, repository.deleteCount)
    }

    @Test
    fun `a switch the store would not write is reported as its own kind of problem`() = runTest {
        // Not as a failed delete. Nothing was removed, the switch has simply snapped back to what
        // the store still holds, and telling somebody their alarm could not be deleted when they
        // only tried to silence it would be alarming and wrong.
        val repository = FakeAlarmRepository(
            alarms = listOf(AN_ALARM),
            saveOutcome = Outcome.Error(message = "the disk is gone"),
        )
        val viewModel = AlarmsViewModel(alarmRepository = repository)

        viewModel.setEnabled(alarm = AN_ALARM, enabled = false)

        assertTrue(viewModel.uiState.value.toggleFailed)
        assertFalse(viewModel.uiState.value.deleteFailed)
        // The screen still shows what the store holds, which is the alarm still switched on.
        assertTrue(viewModel.uiState.value.alarms.single().enabled)
    }

    @Test
    fun `the switch failure clears once it has been shown`() = runTest {
        val repository = FakeAlarmRepository(
            alarms = listOf(AN_ALARM),
            saveOutcome = Outcome.Error(message = "the disk is gone"),
        )
        val viewModel = AlarmsViewModel(alarmRepository = repository)
        viewModel.setEnabled(alarm = AN_ALARM, enabled = false)
        assertTrue(viewModel.uiState.value.toggleFailed)

        viewModel.consumeToggleFailed()

        assertFalse(viewModel.uiState.value.toggleFailed)
    }

    private companion object {

        /** An alarm in the store, row id 1, set for half past seven in the morning. */
        private val AN_ALARM = Alarm(
            id = 1L,
            message = "Take the bread out of the freezer",
            hourOfDay = 7,
            minute = 30,
            enabled = true,
            createdAt = 1_000L,
        )

        /** A second alarm, row id 2. */
        private val ANOTHER_ALARM = Alarm(
            id = 2L,
            message = "Leave for the dentist",
            hourOfDay = 14,
            minute = 5,
            enabled = true,
            createdAt = 2_000L,
        )

        /** A third alarm, row id 3 — so a test can delete the *middle* one of three. */
        private val A_THIRD_ALARM = Alarm(
            id = 3L,
            message = "Put the bins out",
            hourOfDay = 21,
            minute = 0,
            enabled = true,
            createdAt = 3_000L,
        )

        /** An alarm the user has already switched off. */
        private val A_SWITCHED_OFF_ALARM = Alarm(
            id = 4L,
            message = "Water the plants",
            hourOfDay = 9,
            minute = 15,
            enabled = false,
            createdAt = 4_000L,
        )
    }
}

/**
 * An alarms store that is really just a list in memory, and that remembers what it was asked to do.
 *
 * It is **live**, the way the real one is: a write through [save] or [delete] shows up in the
 * collector the ViewModel started in its `init`. A snapshot would have been easier and would have
 * hidden the mistakes that matter most to this screen — a list that never updates after a delete, or
 * a switch that only moves in a local copy of the truth.
 *
 * It deliberately does **not** sort. "Earliest time of day first" is `AlarmRepositoryImpl`'s promise
 * and its own test holds it to it; a fake that sorted here would let a ViewModel that re-ordered the
 * list on the way past go unnoticed.
 *
 * [deleteCount] and [saveCount] are as important as [deletedAlarm] and [savedAlarm]. Several tests
 * above are about something **not** happening — a delete that must not reach the store without a
 * confirmation, a switch that must not write when nothing changed — and "the list is unchanged" is a
 * weaker statement than "the store was never asked".
 *
 * @param alarms the alarms the fake store starts with.
 * @param saveOutcome what [save] reports back. Defaults to success.
 * @param deleteOutcome what [delete] reports back. Defaults to success. An error here is the store
 *   refusing — the row was already gone, or the disk would not write — and the screen has to pass
 *   that on rather than announcing a deletion that never happened.
 * @param deleteGate when given, [delete] **waits** on it before doing anything. That is what lets a
 *   test stand inside the moment a real delete is in flight, which is the only moment the
 *   double-tap guard can be observed. Left null, the store answers straight away.
 * @author Phong-Kaster
 */
private class FakeAlarmRepository(
    alarms: List<Alarm> = emptyList(),
    private val saveOutcome: Outcome<Unit> = Outcome.Success(Unit),
    private val deleteOutcome: Outcome<Unit> = Outcome.Success(Unit),
    private val deleteGate: CompletableDeferred<Unit>? = null,
) : AlarmRepository {

    private val storedAlarms = MutableStateFlow(alarms)

    /** The last alarm [save] was given, exactly as it was given. */
    var savedAlarm: Alarm? = null
        private set

    /** How many times [save] was called. */
    var saveCount: Int = 0
        private set

    /** The last alarm [delete] was given. */
    var deletedAlarm: Alarm? = null
        private set

    /** How many times [delete] was called. */
    var deleteCount: Int = 0
        private set

    override val alarmsFlow: Flow<List<Alarm>> = storedAlarms

    /**
     * Pushes a new list, the way the real store does when something changes somewhere else.
     *
     * @param alarms the list the screen should now be showing.
     */
    fun emit(alarms: List<Alarm>) {
        storedAlarms.value = alarms
    }

    // The Alarms screen never reads one alarm by id — it has the whole list — so this is here to
    // satisfy the interface and nothing more.
    override suspend fun getAlarm(id: Long): Outcome<Alarm?> =
        Outcome.Success(storedAlarms.value.firstOrNull { alarm -> alarm.id == id })

    override suspend fun save(alarm: Alarm): Outcome<Unit> {
        savedAlarm = alarm
        saveCount++

        // A refused write changes nothing, which is the whole reason the screen has to be told about
        // it: the switch snaps back to the state the store still holds.
        if (saveOutcome !is Outcome.Success) return saveOutcome

        storedAlarms.value = storedAlarms.value.map { stored ->
            if (stored.id == alarm.id) alarm else stored
        }
        return saveOutcome
    }

    override suspend fun delete(alarm: Alarm): Outcome<Unit> {
        deleteGate?.await()

        deletedAlarm = alarm
        deleteCount++

        if (deleteOutcome !is Outcome.Success) return deleteOutcome

        storedAlarms.value = storedAlarms.value.filterNot { stored -> stored.id == alarm.id }
        return deleteOutcome
    }
}
