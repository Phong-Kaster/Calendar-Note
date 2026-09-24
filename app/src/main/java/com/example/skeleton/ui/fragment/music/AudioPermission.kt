package com.example.skeleton.ui.fragment.music

/** Same value as `Manifest.permission.READ_MEDIA_AUDIO` (Android 13+). */
const val PERMISSION_READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO"

/** Same value as `Manifest.permission.READ_EXTERNAL_STORAGE` (Android 12L and older). */
const val PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"

/** Android 13 (TIRAMISU) is the first version with the READ_MEDIA_AUDIO permission. */
private const val SDK_TIRAMISU = 33

/**
 * Picks the permission needed to read music on a given Android version.
 * Plain Kotlin (no android.* imports) so it can be unit-tested on the JVM.
 *
 * Example: `audioPermissionFor(sdkInt = Build.VERSION.SDK_INT)`
 * - 33 or newer → "android.permission.READ_MEDIA_AUDIO"
 * - 32 or older → "android.permission.READ_EXTERNAL_STORAGE"
 * @param sdkInt The device API level.
 * @author Phong-Kaster
 */
fun audioPermissionFor(sdkInt: Int): String {
    if (sdkInt >= SDK_TIRAMISU) return PERMISSION_READ_MEDIA_AUDIO
    return PERMISSION_READ_EXTERNAL_STORAGE
}

/** Same value as `Manifest.permission.POST_NOTIFICATIONS` (Android 13+). */
const val PERMISSION_POST_NOTIFICATIONS = "android.permission.POST_NOTIFICATIONS"

/**
 * Whether to ask for the notification permission before playing, so the media notification
 * (the player controls in the notification shade) can show.
 *
 * Android says media notifications are exempt from this permission, but some phones (seen on a
 * vivo, Android 16) block every notification of an app that does not hold it — the player
 * controls then never appear. So the app asks once, on the first play.
 * Plain Kotlin (no android.* imports) so it can be unit-tested on the JVM.
 *
 * Example: `shouldAskNotificationPermission(sdkInt = 34, isGranted = false, alreadyAsked = false)` → true
 * @param sdkInt The device API level.
 * @param isGranted True when the permission is already granted.
 * @param alreadyAsked True when the dialog was already shown on this screen.
 * @author Phong-Kaster
 */
fun shouldAskNotificationPermission(sdkInt: Int, isGranted: Boolean, alreadyAsked: Boolean): Boolean {
    if (sdkInt < SDK_TIRAMISU) return false
    return !isGranted && !alreadyAsked
}
