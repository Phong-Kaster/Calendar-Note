package com.example.skeleton.ui.fragment.music.component

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

/** Android 13 (TIRAMISU): the first version that needs a permission to show notifications. */
private const val NOTIFICATION_PERMISSION_MIN_SDK = 33

/** Written as text because `Manifest.permission.POST_NOTIFICATIONS` only exists on API 33+. */
private const val POST_NOTIFICATIONS_PERMISSION = "android.permission.POST_NOTIFICATIONS"

/**
 * Answers: should we show the "allow notifications" dialog now?
 * Only on Android 13+, only when it is not granted yet, and only once per screen visit.
 *
 * Example:
 * ```kotlin
 * shouldAskNotificationPermission(sdkInt = 33, isGranted = false, hasAskedThisVisit = false) // true
 * shouldAskNotificationPermission(sdkInt = 30, isGranted = false, hasAskedThisVisit = false) // false
 * ```
 *
 * @param sdkInt The phone's Android version number (`Build.VERSION.SDK_INT`).
 * @param isGranted True when notifications are already allowed.
 * @param hasAskedThisVisit True when we already asked since the screen opened.
 * @author Phong-Kaster
 */
fun shouldAskNotificationPermission(sdkInt: Int, isGranted: Boolean, hasAskedThisVisit: Boolean): Boolean {
    if (sdkInt < NOTIFICATION_PERMISSION_MIN_SDK) return false
    if (isGranted) return false
    return !hasAskedThisVisit
}

/*
 * --- How the notification permission is asked (simple story) ---
 * 1. The Fragment bumps requestTrigger by one on every song tap. Playback does NOT wait for us:
 *    the song starts at the same time, whatever the user answers.
 * 2. The number the trigger had when this composable appeared is an old tap: we ignore it
 *    (lastHandledTrigger), so coming back to the screen never pops the dialog by itself.
 * 3. On a new tap, shouldAskNotificationPermission(...) decides. hasAskedThisVisit lives in a
 *    plain remember, so it resets each time the screen is shown again ("once per visit").
 * 4. Below Android 13 nothing happens: notifications need no permission there.
 * This composable draws nothing. It lives next to MusicLayout in the Fragment.
 */
/**
 * Invisible helper that asks for the notification permission (Android 13+) after a song tap.
 *
 * Example:
 * ```kotlin
 * MusicNotificationPermissionRequest(requestTrigger = triggerNotificationPermission)
 * ```
 *
 * @param requestTrigger Increase by one on each song tap; the first value seen is ignored.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MusicNotificationPermissionRequest(requestTrigger: Int) {
    // Plain remember on purpose: a fresh view is a fresh visit and starts from the given trigger.
    var lastHandledTrigger by remember { mutableIntStateOf(requestTrigger) }
    var hasAskedThisVisit by remember { mutableStateOf(false) }
    val permissionState = rememberPermissionState(permission = POST_NOTIFICATIONS_PERMISSION)

    LaunchedEffect(requestTrigger) {
        if (requestTrigger <= lastHandledTrigger) return@LaunchedEffect
        lastHandledTrigger = requestTrigger
        val shouldAsk = shouldAskNotificationPermission(
            sdkInt = Build.VERSION.SDK_INT,
            isGranted = permissionState.status.isGranted,
            hasAskedThisVisit = hasAskedThisVisit,
        )
        if (!shouldAsk) return@LaunchedEffect
        hasAskedThisVisit = true
        permissionState.launchPermissionRequest()
    }
}
