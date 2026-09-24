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
