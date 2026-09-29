package com.example.skeleton.ui.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {
    /**
     * Checks if a specific permission is granted.
     *
     * @param context The application context.
     * @param permission The permission to check (e.g., Manifest.permission.CAMERA).
     * @return True if the permission is granted, false otherwise.
     *
     * Usage example:
     * ```kotlin
     * if (PermissionUtil.isPermissionGranted(this, Manifest.permission.READ_CONTACTS)) {
     *     // Permission is granted, proceed with the operation
     * } else {
     *     // Permission is not granted, request it
     * }
     * ```
     */
    private fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Tells which permission we must ask for to read songs on this phone.
     * Android 13+ (API 33) uses READ_MEDIA_AUDIO; older phones use READ_EXTERNAL_STORAGE.
     *
     * Example:
     * ```kotlin
     * val permissionState = rememberPermissionState(PermissionUtil.audioPermission())
     * ```
     *
     * @return the Manifest permission name to request for audio files.
     * @author Phong-Kaster
     */
    fun audioPermission(): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
    }

    /**
     * Returns true when the app may read songs on this phone.
     *
     * Example:
     * ```kotlin
     * if (PermissionUtil.isAudioPermissionGranted(context)) viewModel.loadSongs()
     * ```
     *
     * @param context any context.
     * @return true if [audioPermission] is granted.
     * @author Phong-Kaster
     */
    fun isAudioPermissionGranted(context: Context): Boolean {
        return isPermissionGranted(context = context, permission = audioPermission())
    }
}