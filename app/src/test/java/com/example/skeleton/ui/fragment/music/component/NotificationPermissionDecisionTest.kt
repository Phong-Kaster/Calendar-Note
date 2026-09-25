package com.example.skeleton.ui.fragment.music.component

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks when the Music screen asks for the notification permission after a song tap.
 * @author Phong-Kaster
 */
class NotificationPermissionDecisionTest {

    @Test
    fun android13NotGrantedNotAsked_asks() {
        assertTrue(shouldAskNotificationPermission(sdkInt = 33, isGranted = false, hasAskedThisVisit = false))
    }

    @Test
    fun android13AlreadyAskedThisVisit_doesNotAsk() {
        assertFalse(shouldAskNotificationPermission(sdkInt = 33, isGranted = false, hasAskedThisVisit = true))
    }

    @Test
    fun android13AlreadyGranted_doesNotAsk() {
        assertFalse(shouldAskNotificationPermission(sdkInt = 34, isGranted = true, hasAskedThisVisit = false))
    }

    @Test
    fun belowAndroid13_neverAsks() {
        assertFalse(shouldAskNotificationPermission(sdkInt = 32, isGranted = false, hasAskedThisVisit = false))
    }
}
