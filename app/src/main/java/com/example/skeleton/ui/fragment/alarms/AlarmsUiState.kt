package com.example.skeleton.ui.fragment.alarms

import com.example.skeleton.domain.model.Alarm

/**
 * Everything the Alarms screen draws.
 *
 * There is exactly one field holding *data* — the alarms themselves. Whether the screen is empty is
 * not stored beside them but **worked out from them**, and that is the whole point of [isEmpty] being
 * a computed `val` rather than a constructor parameter: two fields that describe the same fact are
 * two fields that can disagree, and the way they disagree here is the worst possible one. A list that
 * arrived while `isEmpty` stayed true would draw "No alarms yet" over alarms the user had just
 * written, and nothing in the code would look wrong.
 *
 * @param alarms every alarm the store holds, **already in order** — earliest time of day first.
 *   The ordering is the store's promise (see `AlarmRepository.alarmsFlow`), so nothing on this
 *   screen sorts it. Empty until the first collection arrives, which is why the screen's first
 *   frame is the empty state rather than a blank rectangle.
 * @param pendingDeleteId the row id of the alarm the user has asked to delete and has **not yet
 *   confirmed**, or null when no question is on screen.
 *
 *   **The confirmation is a field here rather than a dialog the screen owns, and that is what makes
 *   it checkable.** Deleting is the only thing in this app that cannot be undone. Kept as state, the
 *   rule "no alarm goes without a second tap" is a fact about `AlarmsViewModel` that a plain unit
 *   test can hold it to — `askToDelete` only writes this field, and `confirmDelete` refuses to do
 *   anything while it is null. Kept as a dialog the layout opens, it would be a habit of one
 *   composable, and the next screen to reuse this ViewModel could wire a delete straight to a row.
 *
 *   An **id** and not a plain `true`/`false`, because the question names one particular alarm: a
 *   boolean would leave `confirmDelete` guessing which row was meant, and "the one that is
 *   highlighted" is not something a store can be told.
 * @param deletedTrigger incremented once each time a delete succeeds. The Fragment watches it, says
 *   so, and clears it back to zero with `consumeDeleted()` — this screen does not leave after a
 *   delete the way the note editor does, so a stale positive count would replay the "Alarm deleted"
 *   toast on the next fresh composition (a rotation, or a return from the editor) for nothing that
 *   just happened. A counter rather than a flag because it is an event, and an event that happens
 *   twice must be visible twice.
 * @param deleteFailed true when the last delete did not happen — the row was already gone, or the
 *   store would not write. Cleared by `consumeDeleteFailed()` once the Fragment has shown it.
 * @param toggleFailed true when switching an alarm on or off did not stick. Separate from
 *   [deleteFailed] because the two are different sentences: nothing was removed here, the switch
 *   simply snapped back to what the store still holds, and telling the user "could not be deleted"
 *   about it would be alarming and wrong. Cleared by `consumeToggleFailed()`.
 * @author Phong-Kaster
 */
data class AlarmsUiState(
    val alarms: List<Alarm> = emptyList(),

    // --- UI control ---
    val pendingDeleteId: Long? = null,

    // --- One-shot events the Fragment consumes ---
    val deletedTrigger: Int = 0,
    val deleteFailed: Boolean = false,
    val toggleFailed: Boolean = false,
) {

    /** True when there is no alarm to show, which is what puts the empty message on screen. */
    val isEmpty: Boolean = alarms.isEmpty()

    /** True while the confirmation sheet is on screen — that is, while an alarm is waiting on it. */
    val confirmingDelete: Boolean = pendingDeleteId != null
}
