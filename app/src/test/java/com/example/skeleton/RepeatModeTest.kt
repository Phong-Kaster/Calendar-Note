package com.example.skeleton

import com.example.skeleton.domain.model.RepeatMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks that the repeat button cycles Off -> All -> One -> Off.
 *
 * @author Phong-Kaster
 */
class RepeatModeTest {

    @Test
    fun off_next_isAll() {
        assertEquals(RepeatMode.All, RepeatMode.Off.next())
    }

    @Test
    fun all_next_isOne() {
        assertEquals(RepeatMode.One, RepeatMode.All.next())
    }

    @Test
    fun one_next_isOff() {
        assertEquals(RepeatMode.Off, RepeatMode.One.next())
    }

    @Test
    fun threeTaps_returnToStart() {
        RepeatMode.entries.forEach { mode ->
            assertEquals(mode, mode.next().next().next())
        }
    }
}
