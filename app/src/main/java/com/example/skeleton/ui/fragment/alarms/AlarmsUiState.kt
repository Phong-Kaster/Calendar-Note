package com.example.skeleton.ui.fragment.alarms

import com.example.skeleton.domain.model.Alarm

/**
 * Everything the Alarms screen draws.
 *
 * There is exactly one *stored* field — the alarms themselves. Whether the screen is empty is not
 * stored beside them but **worked out from them**, and that is the whole point of [isEmpty] being a
 * computed `val` rather than a constructor parameter: two fields that describe the same fact are two
 * fields that can disagree, and the way they disagree here is the worst possible one. A list that
 * arrived while `isEmpty` stayed true would draw "No alarms yet" over alarms the user had just
 * written, and nothing in the code would look wrong.
 *
 * @param alarms every alarm the store holds, **already in order** — earliest time of day first.
 *   The ordering is the store's promise (see `AlarmRepository.alarmsFlow`), so nothing on this
 *   screen sorts it. Empty until the first collection arrives, which is why the screen's first
 *   frame is the empty state rather than a blank rectangle.
 * @author Phong-Kaster
 */
data class AlarmsUiState(
    val alarms: List<Alarm> = emptyList(),
) {

    /** True when there is no alarm to show, which is what puts the empty message on screen. */
    val isEmpty: Boolean = alarms.isEmpty()
}
