package com.example.skeleton.ui.fragment.music

import org.junit.Assert.assertEquals
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
}
