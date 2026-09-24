package com.example.skeleton.ui.fragment.music

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks which audio permission is picked per Android version.
 * @author Phong-Kaster
 */
class AudioPermissionTest {

    @Test
    fun android13AndNewer_useReadMediaAudio() {
        listOf(33, 34, 36).forEach { sdkInt ->
            assertEquals("sdk $sdkInt", "android.permission.READ_MEDIA_AUDIO", audioPermissionFor(sdkInt = sdkInt))
        }
    }

    @Test
    fun android12LAndOlder_useReadExternalStorage() {
        listOf(24, 29, 32).forEach { sdkInt ->
            assertEquals("sdk $sdkInt", "android.permission.READ_EXTERNAL_STORAGE", audioPermissionFor(sdkInt = sdkInt))
        }
    }

    @Test
    fun android13AndNewer_asksForNotificationsOnce_whenNotGranted() {
        listOf(33, 34, 36).forEach { sdkInt ->
            assertTrue("sdk $sdkInt", shouldAskNotificationPermission(sdkInt = sdkInt, isGranted = false, alreadyAsked = false))
            assertFalse("sdk $sdkInt asked", shouldAskNotificationPermission(sdkInt = sdkInt, isGranted = false, alreadyAsked = true))
            assertFalse("sdk $sdkInt granted", shouldAskNotificationPermission(sdkInt = sdkInt, isGranted = true, alreadyAsked = false))
        }
    }

    @Test
    fun android12LAndOlder_neverAsksForNotifications() {
        listOf(24, 29, 32).forEach { sdkInt ->
            assertFalse("sdk $sdkInt", shouldAskNotificationPermission(sdkInt = sdkInt, isGranted = false, alreadyAsked = false))
        }
    }
}
