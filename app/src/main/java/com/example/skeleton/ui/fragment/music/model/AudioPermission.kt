package com.example.skeleton.ui.fragment.music.model

/** Android 13 (API 33): the first version that uses READ_MEDIA_AUDIO. */
private const val SDK_TIRAMISU = 33

/** Permission name used on Android 13 and newer. */
const val PERMISSION_READ_MEDIA_AUDIO = "android.permission.READ_MEDIA_AUDIO"

/** Permission name used on Android 12L and older. */
const val PERMISSION_READ_EXTERNAL_STORAGE = "android.permission.READ_EXTERNAL_STORAGE"

/**
 * Tells which permission the app must ask for to read songs on a phone running [sdkInt].
 *
 * Android 13+ has a special "music and audio" permission; older phones use the general
 * storage permission. The SDK number is a parameter so this can be tested on a plain JVM.
 *
 * Example:
 * ```kotlin
 * audioPermissionFor(sdkInt = Build.VERSION.SDK_INT)
 * audioPermissionFor(sdkInt = 34) // "android.permission.READ_MEDIA_AUDIO"
 * audioPermissionFor(sdkInt = 30) // "android.permission.READ_EXTERNAL_STORAGE"
 * ```
 *
 * @param sdkInt The Android API level of the phone.
 * @return the permission name as a String.
 * @author Phong-Kaster
 */
fun audioPermissionFor(sdkInt: Int): String {
    if (sdkInt >= SDK_TIRAMISU) return PERMISSION_READ_MEDIA_AUDIO
    return PERMISSION_READ_EXTERNAL_STORAGE
}

/** How many "not granted" answers we need to see before we believe the denial is permanent. */
private const val DENIED_ANSWERS_BEFORE_SETTINGS = 2

/**
 * What one tap on "Grant permission" should do.
 *
 * @author Phong-Kaster
 */
enum class GrantPermissionTapAction {
    /** The permission is already on: just tell the screen. */
    ReportGranted,

    /** Show the Android permission dialog. */
    ShowSystemDialog,

    /** Android will not show the dialog any more: open this app's page in the phone Settings. */
    OpenSettings,
}

/*
 * --- Why we count "not granted" answers (simple story) ---
 * Android never says "the user picked Don't ask again". We have to guess from two clues:
 * - shouldShowRationale is true right after the user said "Deny" once.
 * - After a second "Deny" (or ticking "Don't ask again") it turns false again for good.
 * Closing the dialog without answering (tap outside / Back) also gives "not granted", but
 * shouldShowRationale stays false, exactly like a phone that never asked. So one answer with
 * no rationale is NOT enough to call the denial permanent - we need at least two answers.
 * Every "not granted" answer counts, even a closed dialog, so a denial that was already
 * permanent before the app restarted still reaches Settings after two taps instead of never.
 */
/**
 * Decides what a tap on "Grant permission" does, from plain values only (easy to unit test).
 *
 * Example:
 * ```kotlin
 * grantPermissionTapAction(isGranted = false, shouldShowRationale = true, deniedAnswerCount = 1)
 * // ShowSystemDialog: the user said "Deny" once, Android still lets us ask.
 * grantPermissionTapAction(isGranted = false, shouldShowRationale = false, deniedAnswerCount = 2)
 * // OpenSettings: the user refused twice, the dialog will not appear any more.
 * ```
 *
 * @param isGranted True when the permission is already on.
 * @param shouldShowRationale Android's "the user denied once, you may ask again" flag.
 * @param deniedAnswerCount How many times a permission request came back "not granted".
 * @return the [GrantPermissionTapAction] to perform.
 * @author Phong-Kaster
 */
fun grantPermissionTapAction(
    isGranted: Boolean,
    shouldShowRationale: Boolean,
    deniedAnswerCount: Int,
): GrantPermissionTapAction {
    if (isGranted) return GrantPermissionTapAction.ReportGranted
    if (shouldShowRationale) return GrantPermissionTapAction.ShowSystemDialog
    if (deniedAnswerCount < DENIED_ANSWERS_BEFORE_SETTINGS) return GrantPermissionTapAction.ShowSystemDialog
    return GrantPermissionTapAction.OpenSettings
}
