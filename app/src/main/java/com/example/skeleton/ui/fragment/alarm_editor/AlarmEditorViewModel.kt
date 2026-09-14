package com.example.skeleton.ui.fragment.alarm_editor

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.enums.AlarmRepeatMode
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.model.BlankAlarmMessageException
import com.example.skeleton.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek

/**
 * Drives the alarm editor: holds what the user is typing and picking, and hands it to the store on
 * save.
 *
 * This ViewModel has no `init` block, and that is deliberate. Every top-level screen in this app
 * knows what to load the moment it exists; this one does not — it has to be *told* which alarm to
 * open, because that arrives as a navigation argument. So the Fragment calls [openAlarm] once, and
 * until it does there is no alarm here at all.
 *
 * @param alarmRepository the alarms store, injected by interface so a test can hand over a fake.
 * @author Phong-Kaster
 */
class AlarmEditorViewModel(
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    private val TAG = "AlarmEditorViewModel"

    private val _uiState = MutableStateFlow(AlarmEditorUiState())
    val uiState: StateFlow<AlarmEditorUiState> = _uiState.asStateFlow()

    /**
     * The alarm as it was when the screen opened — a fresh draft, or the stored alarm being edited.
     *
     * It is kept whole rather than as loose fields because its `id` and its `createdAt` both have to
     * survive back into the store untouched at save time, and an alarm that loses either one turns
     * into a *second* alarm the next time it is saved.
     *
     * **`null` means there is nothing to write, and that is load-bearing.** It is null in two
     * situations: before [openAlarm] has run, and when opening failed because the alarm was removed
     * elsewhere or the store would not answer. [save] stops on it. Without the second case, a screen
     * that failed to open would still be holding a plausible-looking blank alarm, and one tap on
     * Save would file it as a brand-new one beside the original the user was trying to edit — the
     * exact defect the note editor was fixed for.
     */
    private var openedAlarm: Alarm? = null

    /**
     * Guards [openAlarm] against running twice.
     *
     * Not paranoia: a Fragment is recreated on every rotation while its ViewModel survives, so a
     * second call is the normal course of events — and it would overwrite whatever the user had
     * typed with what the database still holds.
     */
    private var opened = false

    /**
     * Guards [save] against a double tap inserting the same new alarm twice.
     *
     * It is **never** lowered again after a save succeeds, and that asymmetry is the whole point. A
     * successful save is followed by the screen leaving — but leaving is animated, and the
     * composition stays alive and accepting touches for the length of that animation. A guard that
     * reopened the moment the database returned would be shut for a few milliseconds and open for
     * the next few hundred, which is exactly the window a second tap lands in. The second tap would
     * then insert a *second* row, because an alarm that has just been created still carries
     * [Alarm.UNSAVED_ID] here — this ViewModel never learns the id the store handed out.
     */
    private var saving = false

    /**
     * Loads the alarm this screen was opened for.
     *
     * The two branches are deliberately different shapes. A brand-new alarm needs no store and no
     * coroutine, so it is set up **synchronously**, before this function returns. An existing one
     * has to be read, and [AlarmEditorUiState.isLoading] is raised synchronously too — see that
     * field's own comment for why the editor must not be drawn before the answer arrives.
     *
     * @param alarmId the row id to edit, or [Alarm.UNSAVED_ID] to start a new alarm.
     */
    fun openAlarm(alarmId: Long) {
        if (opened) return
        opened = true

        if (alarmId == Alarm.UNSAVED_ID) {
            val draft = Alarm.draft()
            openedAlarm = draft
            _uiState.value = _uiState.value.copy(
                message = draft.message,
                hourOfDay = draft.hourOfDay,
                minute = draft.minute,
                repeatMode = draft.repeatMode,
                repeatDays = draft.repeatDays,
            )
            return
        }

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            val outcome = alarmRepository.getAlarm(id = alarmId)

            // Not a successful read at all: the store could not be asked. `Loading` lands here too
            // and that is correct — `getAlarm` never returns it, so seeing one would mean something
            // has changed underneath and the honest answer is still "I do not know".
            //
            // A successful read of `null` lands in the same place, one line down, and for this
            // screen the two lead to the same behaviour: there is nothing here to edit, so say so
            // and leave. They are still asked in the right order, because a caller can now reach
            // this screen with a real id — a tap on a row in the Alarms list — so the difference is
            // one deletion feature away from mattering.
            val stored = (outcome as? Outcome.Success)?.data
            if (stored == null) {
                Log.w(TAG, "openAlarm($alarmId) found no alarm to edit")
                _uiState.value = _uiState.value.copy(isLoading = false, openFailed = true)
                return@launch
            }

            openedAlarm = stored
            _uiState.value = _uiState.value.copy(
                message = stored.message,
                hourOfDay = stored.hourOfDay,
                minute = stored.minute,
                repeatMode = stored.repeatMode,
                repeatDays = stored.repeatDays,
                isLoading = false,
            )
        }
    }

    /** Mirrors the reminder text the user is typing. */
    fun setMessage(value: String) {
        _uiState.value = _uiState.value.copy(message = value)
    }

    /**
     * Mirrors the time the user picked.
     *
     * @param hourOfDay the hour, 0–23.
     * @param minute the minute past the hour, 0–59.
     */
    fun setTime(hourOfDay: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(hourOfDay = hourOfDay, minute = minute)
    }

    /**
     * Mirrors which repeat option the user picked.
     *
     * [AlarmEditorUiState.repeatDays] is left exactly as it was, even when leaving `CUSTOM` —
     * switching to `CUSTOM` a second time shows the same ticks rather than an empty row, which
     * matches [Alarm.repeatDays]'s own contract.
     */
    fun setRepeatMode(repeatMode: AlarmRepeatMode) {
        _uiState.value = _uiState.value.copy(repeatMode = repeatMode)
    }

    /**
     * Flips one weekday on or off for a `CUSTOM` alarm.
     *
     * Meaningful only while [AlarmEditorUiState.repeatMode] is [AlarmRepeatMode.CUSTOM] — the row of
     * weekday toggles this drives is not even drawn otherwise — but it does not guard on that here.
     * The set is still exactly what would be saved if the user switched to Custom this instant, and
     * refusing the toggle for the wrong reason (a stale `repeatMode` read at the wrong moment) is a
     * worse bug than a toggle that changed a number nothing currently reads.
     *
     * @param day the weekday tapped.
     */
    fun toggleRepeatDay(day: DayOfWeek) {
        val current = _uiState.value.repeatDays
        val next = if (day in current) current - day else current + day
        _uiState.value = _uiState.value.copy(repeatDays = next)
    }

    /**
     * Hands the alarm to the store, keeping its id and its original creation time.
     *
     * The timestamp is not set here. The store owns the clock — see [AlarmRepository.save] — so this
     * passes the alarm along as it stands and lets the one place that knows what time it is do the
     * stamping.
     *
     * **The blank-message check is not repeated here either**, even though this screen could see it
     * coming. The rule belongs to the store, where every caller reaches it; checking twice would
     * mean two places to change it and one of them getting missed.
     */
    fun save() {
        // Nothing was ever opened, or opening failed. There is no alarm to write, and writing the
        // typed text as a new one would be the duplicate this screen exists not to make.
        val alarm = openedAlarm ?: return
        if (saving) return
        saving = true

        viewModelScope.launch {
            val state = _uiState.value
            val outcome = alarmRepository.save(
                alarm = alarm.copy(
                    message = state.message,
                    hourOfDay = state.hourOfDay,
                    minute = state.minute,
                    repeatMode = state.repeatMode,
                    // Stored regardless of `repeatMode`, same as the screen carries it regardless —
                    // see `Alarm.repeatDays`'s own KDoc for why an alarm switched away from Custom
                    // keeps its ticks rather than losing them.
                    repeatDays = state.repeatDays,
                ),
            )

            if (outcome is Outcome.Success) {
                // `saving` deliberately stays true — see its own comment. The screen is leaving.
                _uiState.value = _uiState.value.copy(
                    savedTrigger = _uiState.value.savedTrigger + 1,
                )
                return@launch
            }

            // Nothing was stored, so the user is still here with their text and the button has to
            // work again. True of both branches below — a refusal and a failure alike.
            saving = false

            // **Which kind of "no" this was decides what the user is told, and the store says so by
            // type.** A refused alarm has nothing written on it; tapping Save again changes
            // nothing, so "something went wrong, please try again" would be an instruction that
            // cannot work. A write that merely failed *is* worth another tap.
            //
            // The store's own `message` is developer-facing and is deliberately not shown: the
            // screen picks its own wording, and matching on that text to decide which wording to
            // pick would break the next time somebody rephrased a log line.
            val refusal = (outcome as? Outcome.Error)?.throwable as? BlankAlarmMessageException
            if (refusal != null) {
                Log.w(TAG, "save refused: the alarm has a blank message")
                _uiState.value = _uiState.value.copy(saveRefusedBlank = true)
                return@launch
            }

            Log.w(TAG, "save failed: ${(outcome as? Outcome.Error)?.message}")
            _uiState.value = _uiState.value.copy(saveFailed = true)
        }
    }

    /** Clears [AlarmEditorUiState.saveFailed] once the Fragment has shown the message. */
    fun consumeSaveFailed() {
        _uiState.value = _uiState.value.copy(saveFailed = false)
    }

    /** Clears [AlarmEditorUiState.saveRefusedBlank] once the Fragment has shown the message. */
    fun consumeSaveRefused() {
        _uiState.value = _uiState.value.copy(saveRefusedBlank = false)
    }

    /** Clears [AlarmEditorUiState.openFailed] once the Fragment has shown the message. */
    fun consumeOpenFailed() {
        _uiState.value = _uiState.value.copy(openFailed = false)
    }
}
