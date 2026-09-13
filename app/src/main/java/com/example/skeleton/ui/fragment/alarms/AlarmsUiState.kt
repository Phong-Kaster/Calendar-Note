package com.example.skeleton.ui.fragment.alarms

/**
 * Everything the Alarms screen draws.
 *
 * There is only one field, and that is honest rather than lazy: nothing is stored yet. No alarm is
 * saved anywhere, no `Alarm` domain model exists, and no repository has been written — so inventing
 * a `List<Alarm>` here would be a promise this screen cannot keep. When alarms really are stored,
 * this class grows a list and [isEmpty] becomes a derived `val` over it.
 *
 * @param isEmpty true when the screen has no alarms to show, which right now is always. The screen
 *   draws its empty message on this and nothing else.
 * @author Phong-Kaster
 */
data class AlarmsUiState(
    val isEmpty: Boolean = true,
)
