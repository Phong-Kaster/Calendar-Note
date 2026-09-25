package com.example.skeleton.ui.fragment.music.model

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks that the right audio permission is picked for each Android version.
 *
 * @author Phong-Kaster
 */
class AudioPermissionTest {

    @Test
    fun api33_usesReadMediaAudio() {
        assertEquals("android.permission.READ_MEDIA_AUDIO", audioPermissionFor(sdkInt = 33))
    }

    @Test
    fun api34_usesReadMediaAudio() {
        assertEquals("android.permission.READ_MEDIA_AUDIO", audioPermissionFor(sdkInt = 34))
    }

    @Test
    fun api32_usesReadExternalStorage() {
        assertEquals("android.permission.READ_EXTERNAL_STORAGE", audioPermissionFor(sdkInt = 32))
    }

    @Test
    fun api24_usesReadExternalStorage() {
        assertEquals("android.permission.READ_EXTERNAL_STORAGE", audioPermissionFor(sdkInt = 24))
    }

    @Test
    fun tap_whenGranted_reportsGranted() {
        assertEquals(
            GrantPermissionTapAction.ReportGranted,
            grantPermissionTapAction(isGranted = true, shouldShowRationale = false, deniedAnswerCount = 5),
        )
    }

    @Test
    fun tap_neverAsked_showsSystemDialog() {
        assertEquals(
            GrantPermissionTapAction.ShowSystemDialog,
            grantPermissionTapAction(isGranted = false, shouldShowRationale = false, deniedAnswerCount = 0),
        )
    }

    @Test
    fun tap_afterDialogClosedWithoutAnswer_showsSystemDialogAgain() {
        // Dismissed once: not granted, but Android gives no rationale. Not permanent yet.
        assertEquals(
            GrantPermissionTapAction.ShowSystemDialog,
            grantPermissionTapAction(isGranted = false, shouldShowRationale = false, deniedAnswerCount = 1),
        )
    }

    @Test
    fun tap_afterFirstRefusal_showsSystemDialogAgain() {
        assertEquals(
            GrantPermissionTapAction.ShowSystemDialog,
            grantPermissionTapAction(isGranted = false, shouldShowRationale = true, deniedAnswerCount = 1),
        )
    }

    @Test
    fun tap_afterRefusalThenDismiss_showsSystemDialogAgain() {
        // Rationale still true means Android will still show the dialog.
        assertEquals(
            GrantPermissionTapAction.ShowSystemDialog,
            grantPermissionTapAction(isGranted = false, shouldShowRationale = true, deniedAnswerCount = 2),
        )
    }

    @Test
    fun tap_afterSecondRefusal_opensSettings() {
        assertEquals(
            GrantPermissionTapAction.OpenSettings,
            grantPermissionTapAction(isGranted = false, shouldShowRationale = false, deniedAnswerCount = 2),
        )
    }
}
