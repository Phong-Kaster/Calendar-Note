package com.example.skeleton.domain.scheduler

import java.time.DayOfWeek

/**
 * Packs a set of weekdays into the bits of a single `Int`, and back.
 *
 * Android `Intent` extras can only carry a handful of primitive shapes, and `Set<DayOfWeek>` is not
 * one of them — so `AlarmManagerAlarmScheduler` (arming an alarm) and `AlarmReceiver` (rebuilding one
 * out of the intent it fired from) both need the same translation between a custom-repeat alarm's
 * weekdays and something an intent can actually carry. It lives in exactly one place, in `domain/`
 * with no Android in sight, so the two call sites can never disagree about which bit means which day
 * and so the mapping is checkable by an ordinary JVM test.
 *
 * Bit `dayOfWeek.value - 1` is set when that day is included — Monday is bit 0, Sunday is bit 6,
 * following `DayOfWeek.value`'s own ISO-8601 numbering (Monday = 1 … Sunday = 7).
 *
 * @author Phong-Kaster
 */
fun Set<DayOfWeek>.toRepeatDaysBitmask(): Int =
    fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

/** The exact inverse of [toRepeatDaysBitmask]. */
fun Int.toRepeatDaySet(): Set<DayOfWeek> =
    DayOfWeek.entries.filterTo(mutableSetOf()) { day -> (this shr (day.value - 1)) and 1 == 1 }
