package com.example.skeleton.domain.scheduler

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.DayOfWeek

/**
 * Holds [toRepeatDaysBitmask] and [toRepeatDaySet] to the one promise that matters: packing a set of
 * weekdays and unpacking it again must return exactly the set that went in, because
 * `AlarmManagerAlarmScheduler` (packing, to put in an intent extra) and `AlarmReceiver` (unpacking,
 * to rebuild the alarm that intent fired from) never run in the same process call — a bit that lands
 * on the wrong day is a custom alarm that quietly fires on a day the user never picked.
 *
 * @author Phong-Kaster
 */
class AlarmRepeatDaysBitmaskTest {

    @Test
    fun `an empty set packs to zero`() {
        assertEquals(0, emptySet<DayOfWeek>().toRepeatDaysBitmask())
    }

    @Test
    fun `zero unpacks to an empty set`() {
        assertEquals(emptySet<DayOfWeek>(), 0.toRepeatDaySet())
    }

    @Test
    fun `monday is bit zero and sunday is bit six`() {
        // Pinned on purpose. `DayOfWeek.value` numbers Monday 1 through Sunday 7 — this is the one
        // place that ISO numbering is translated into a bit position, and a test that only checked
        // round-tripping could not catch the off-by-one of using `.value` directly as the shift.
        assertEquals(0b0000001, setOf(DayOfWeek.MONDAY).toRepeatDaysBitmask())
        assertEquals(0b1000000, setOf(DayOfWeek.SUNDAY).toRepeatDaysBitmask())
    }

    @Test
    fun `every weekday packed together is every bit set`() {
        assertEquals(0b1111111, DayOfWeek.entries.toSet().toRepeatDaysBitmask())
    }

    @Test
    fun `a mixed set survives packing and unpacking unchanged`() {
        val original = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY)

        val roundTripped = original.toRepeatDaysBitmask().toRepeatDaySet()

        assertEquals(original, roundTripped)
    }

    @Test
    fun `unpacking ignores bits above the seven weekdays use`() {
        // Nothing in this app ever sets bit 7 or higher, but `Int` has thirty-two of them, and a
        // corrupted or future-written extra should not resurrect a fictional eighth day.
        val withStrayHighBit = setOf(DayOfWeek.FRIDAY).toRepeatDaysBitmask() or (1 shl 10)

        assertEquals(setOf(DayOfWeek.FRIDAY), withStrayHighBit.toRepeatDaySet())
    }
}
