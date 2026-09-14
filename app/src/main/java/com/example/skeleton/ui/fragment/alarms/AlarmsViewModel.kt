package com.example.skeleton.ui.fragment.alarms

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Holds the Alarms screen's state: every alarm in the store, kept current — and the two things the
 * user can do to an alarm without opening it, which are switching it off and removing it.
 *
 * Writing an alarm's *words* and *time* still happens on the editor screen; there is nothing here
 * for that.
 *
 * **It subscribes rather than fetching**, and that difference is what makes the screen work at all.
 * `alarmsFlow` is a live stream: saving an alarm on the editor makes it emit again on its own, so by
 * the time the user is back here the list already contains what they just wrote — and a delete or a
 * flipped switch redraws the list for the same reason, with nothing here having to nudge it.
 *
 * Nothing here sorts, filters or counts. The order is the store's promise (earliest time of day
 * first) and "is the screen empty?" is a computed `val` on [AlarmsUiState]; doing either job twice
 * would mean two places to keep right.
 *
 * @param alarmRepository the alarms store, injected by interface so a test can hand over a fake.
 * @author Phong-Kaster
 */
class AlarmsViewModel(
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    private val TAG = "AlarmsViewModel"

    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()

    /**
     * Guards [confirmDelete] against a double tap deleting twice.
     *
     * The second tap of a double tap lands while the first delete is still in the database. Both
     * would read the same [AlarmsUiState.pendingDeleteId] — it is only cleared when an answer comes
     * back — so both would reach the store, and the second would match no row. The user would then
     * be told "the alarm could not be deleted" about the alarm they just successfully deleted.
     *
     * Unlike the note editor's guard, this one **is** lowered again once the answer arrives. That
     * screen leaves after a delete; this one stays, and the user may well want to remove a second
     * alarm a moment later.
     */
    private var deleting = false

    init {
        collectAlarms()
    }

    /**
     * Keeps [uiState] level with the store, for as long as this ViewModel lives.
     *
     * `collectLatest` and not `collect`: only the newest list matters here. If two emissions land in
     * the same instant — a save landing while a previous list is still being copied into state — the
     * older one has nothing left to say, and drawing it first would be one frame of a list the store
     * has already replaced.
     *
     * There is no error branch, and there does not need to be one. The store's flow is fail-soft: a
     * database that will not read emits an empty list and logs, rather than throwing up into this
     * collector, which catches nothing. See `AlarmRepositoryImpl.alarmsFlow`.
     */
    private fun collectAlarms() {
        viewModelScope.launch {
            alarmRepository.alarmsFlow.collectLatest { alarms ->
                _uiState.value = _uiState.value.copy(alarms = alarms)
            }
        }
    }

    /**
     * Switches one alarm on or off, and stores the change.
     *
     * It goes through the ordinary [AlarmRepository.save] rather than through a method of its own.
     * Being armed is just a field on the alarm, so flipping it is just an edit — and a second write
     * path would be a second place for the store's write rules to be applied, which is the place
     * that gets missed.
     *
     * The screen is **not** updated from here. The switch draws what `uiState` says, `uiState`
     * mirrors the store, and the store's list emits again the moment the row changes. Setting the
     * flag here as well would put a second copy of the truth on screen, and the copy would be the
     * one that is wrong whenever the write fails.
     *
     * @param alarm the alarm to change, exactly as it came out of the list — its id and its creation
     *   stamp travel back into the store untouched.
     * @param enabled true to arm the alarm, false to switch it off. **Only the flag is persisted.**
     *   Nothing is scheduled here and nothing goes off; ringing is a later job.
     */
    fun setEnabled(alarm: Alarm, enabled: Boolean) {
        // Nothing to do, so nothing is written. A switch redrawn into the position it was already in
        // is not a user changing their mind, and a write for it would be a row touched for nothing.
        if (alarm.enabled == enabled) return

        viewModelScope.launch {
            val outcome = alarmRepository.save(alarm = alarm.copy(enabled = enabled))
            if (outcome is Outcome.Success) return@launch

            // The switch has already snapped back — it draws the store's answer, and the store's
            // answer did not change. Without a word to go with it, that looks like a control that
            // ignores taps.
            Log.w(TAG, "setEnabled(id=${alarm.id}) failed: ${(outcome as? Outcome.Error)?.message}")
            _uiState.value = _uiState.value.copy(toggleFailed = true)
        }
    }

    /**
     * The user asked to delete an alarm. **This does not delete anything.**
     *
     * It remembers which alarm was asked about and stops. Deleting is the only irreversible thing
     * this app does, so the store is unreachable from one tap by construction: the single caller of
     * [confirmDelete] is the confirming button inside the sheet this id opens.
     *
     * @param alarmId the row id the user tapped the delete affordance on.
     */
    fun askToDelete(alarmId: Long) {
        _uiState.value = _uiState.value.copy(pendingDeleteId = alarmId)
    }

    /** The user backed out of the confirmation. Nothing was deleted. */
    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(pendingDeleteId = null)
    }

    /**
     * Removes the alarm the user confirmed. **This is the only path from this screen to a deletion.**
     *
     * The list needs no nudge afterwards: the store's list is a live stream, so the row leaves the
     * screen on its own.
     */
    fun confirmDelete() {
        // **The confirmation is not optional, and this line is what makes that a fact about this
        // class rather than a habit of the layout that calls it.** The sheet is the only caller
        // today, but "the only caller today" is not a guarantee — the next screen to reuse this
        // ViewModel could wire a delete straight to a row, and the loss would be silent and
        // permanent. With this guard, a delete that skipped the confirmation does nothing at all,
        // and a test can say so.
        val alarmId = _uiState.value.pendingDeleteId ?: return
        if (deleting) return
        deleting = true

        viewModelScope.launch {
            // The whole alarm, looked up by the id the user confirmed. The store deletes by id, but
            // it takes an `Alarm` — and taking it from the list the user is looking at means the
            // alarm that goes is the one whose row they tapped, not "the one at that position",
            // which is a different alarm the moment the list re-orders underneath.
            val alarm = _uiState.value.alarms.firstOrNull { candidate -> candidate.id == alarmId }
            val outcome = alarm?.let { target -> alarmRepository.delete(alarm = target) }

            deleting = false

            if (outcome is Outcome.Success) {
                _uiState.value = _uiState.value.copy(
                    pendingDeleteId = null,
                    deletedTrigger = _uiState.value.deletedTrigger + 1,
                )
                return@launch
            }

            // Two ways to land here and one sentence for both: the alarm had already left the list
            // (`alarm` was null, and nothing was asked of the store), or the store refused. Either
            // way nothing was removed, and that is the only thing the user needs to be told.
            Log.w(TAG, "confirmDelete($alarmId) removed nothing: ${(outcome as? Outcome.Error)?.message}")
            _uiState.value = _uiState.value.copy(
                // The confirmation closes either way: leaving it up under a failure message reads as
                // though tapping it again might work.
                pendingDeleteId = null,
                deleteFailed = true,
            )
        }
    }

    /**
     * Clears [AlarmsUiState.deletedTrigger] back to zero once the Fragment has shown it.
     *
     * **This screen does not leave after a delete** — unlike the note editor, whose `deletedTrigger`
     * is safe left unconsumed only because `safeNavigateUp()` tears the whole screen down the same
     * moment. Here the Fragment survives, and so does its ViewModel: a `LaunchedEffect` keyed on the
     * trigger fires the instant its owning composition is entered, not only when the value actually
     * changes from what *that* composition last saw. Leave the trigger sitting above zero and the
     * next fresh composition — a rotation, or a return from the editor after opening a row — replays
     * the "Alarm deleted" toast for an alarm that has not been touched since. Resetting to zero here
     * is what keeps the count meaning "a delete happened since the last time this was shown" rather
     * than "a delete has ever happened".
     */
    fun consumeDeleted() {
        _uiState.value = _uiState.value.copy(deletedTrigger = 0)
    }

    /** Clears [AlarmsUiState.deleteFailed] once the Fragment has shown the message. */
    fun consumeDeleteFailed() {
        _uiState.value = _uiState.value.copy(deleteFailed = false)
    }

    /** Clears [AlarmsUiState.toggleFailed] once the Fragment has shown the message. */
    fun consumeToggleFailed() {
        _uiState.value = _uiState.value.copy(toggleFailed = false)
    }
}
