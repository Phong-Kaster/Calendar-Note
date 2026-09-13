package com.example.skeleton.ui.fragment.alarm_editor

import com.example.skeleton.domain.model.Alarm

/**
 * Everything the alarm editor needs to draw itself.
 *
 * Notice what is *not* here: the alarm's row id, its `createdAt`, and whether it is enabled. None of
 * them is drawn on this screen, so none belongs in UI state — the ViewModel keeps them privately and
 * hands them back to the store at save time. UI state is what a screen paints, not everything a
 * screen knows.
 *
 * There is also no `error` field. A save that fails is a passing event, not a state the screen sits
 * in, so it arrives as [saveFailed] and is cleared the moment it has been shown.
 *
 * @param message the reminder text being typed. May be blank while the user is still thinking —
 *   the *store* refuses a blank one at save time, and the refusal comes back as
 *   [saveRefusedBlank].
 * @param hourOfDay the hour the alarm is set for, 0–23.
 * @param minute the minute past that hour, 0–59.
 * @param isLoading true while an **existing** alarm is being read out of the store. A brand-new
 *   alarm is never loading: there is nothing to read.
 *
 *   **This flag is load-bearing and not decoration.** Material's time control takes its starting
 *   hour and minute at the moment it is first composed and does not follow them afterwards, so an
 *   editor drawn before the alarm arrived would show 08:00 for an alarm set to 21:30 and quietly
 *   save the wrong time. `CoreLayout(showLoading = …)` draws a spinner instead of the content, which
 *   means the editor's first composition is the one that already has the real numbers.
 * @param savedTrigger incremented once each time a save succeeds. The Fragment watches it and
 *   leaves the screen; a counter rather than a flag because it is an event, and an event that
 *   happens twice must be visible twice.
 * @param saveFailed true when the last save **failed** — the store was willing and something went
 *   wrong. The Fragment shows a message and calls `consumeSaveFailed()`, which puts it back to
 *   false. A save the store *refused* does not raise this; see [saveRefusedBlank].
 * @param saveRefusedBlank true when the store declined the alarm because nothing was written on it.
 *   **Separate from [saveFailed] because the two need opposite things said about them:** a failed
 *   write is worth retrying, and a refusal never is, so one message inviting the user to "try again"
 *   for both leaves them tapping Save at an alarm that can never be stored. Unlike the note
 *   editor's refusal, this one carries no value alongside it — "you wrote nothing" is the whole of
 *   the message, and the field the user has to change is the one right in front of them. Cleared by
 *   `consumeSaveRefused()` once shown.
 * @param openFailed true when the screen could not open the alarm it was sent to edit — it has been
 *   removed, or the store would not answer. Either way there is nothing here to edit, so the
 *   Fragment says so and leaves rather than letting the user type into an editor with no alarm
 *   behind it. (The note editor splits these two apart into a `NoteProblem`; here they are one flag
 *   because nothing navigates to this screen with a real id yet. The task that adds "tap a row to
 *   edit" is the one that will find out whether the difference is worth telling the user about.)
 * @author Phong-Kaster
 */
data class AlarmEditorUiState(
    val message: String = "",
    val hourOfDay: Int = Alarm.DEFAULT_HOUR_OF_DAY,
    val minute: Int = Alarm.DEFAULT_MINUTE,

    // --- UI control ---
    val isLoading: Boolean = false,

    // --- One-shot events the Fragment consumes ---
    val savedTrigger: Int = 0,
    val saveFailed: Boolean = false,
    val saveRefusedBlank: Boolean = false,
    val openFailed: Boolean = false,
)
