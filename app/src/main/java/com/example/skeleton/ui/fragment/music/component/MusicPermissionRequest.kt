package com.example.skeleton.ui.fragment.music.component

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.fragment.music.model.GrantPermissionTapAction
import com.example.skeleton.ui.fragment.music.model.audioPermissionFor
import com.example.skeleton.ui.fragment.music.model.grantPermissionTapAction
import com.example.skeleton.ui.theme.customizedTextStyle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

private const val TAG = "MusicPermissionRequest"

/*
 * --- How the audio permission is asked (simple story) ---
 * 1. The first time the screen opens, we ask once automatically.
 * 2. Every time the answer changes (dialog closed, or Accompanist re-checks on resume),
 *    we tell the caller through onPermissionResult.
 * 3. Every "not granted" answer from a request is counted (deniedAnswerCount).
 * 4. When the user taps "Grant permission" (requestTrigger goes up by one),
 *    grantPermissionTapAction(...) picks: report granted, show the system dialog, or - only
 *    after two "not granted" answers and no rationale - open this app's page in Settings.
 * 5. The Fragment keeps requestTrigger alive while this screen sits on the back stack, so
 *    when the screen comes back the old number is still there. We remember the number we
 *    saw when this composable first appeared and ignore it; only a NEW tap (a bigger
 *    number) does anything. Otherwise pressing Back would open Settings on its own.
 * This composable draws nothing. It lives next to MusicLayout in the Fragment.
 */
/**
 * Invisible helper that asks for the audio permission with Accompanist and reports the answer.
 *
 * Example:
 * ```kotlin
 * MusicPermissionRequest(
 *     requestTrigger = triggerRequestPermission,
 *     onPermissionResult = { granted -> viewModel.onPermissionResult(granted = granted) },
 * )
 * ```
 *
 * @param requestTrigger Increase by one to ask again (0 means "user has not tapped yet").
 * @param onPermissionResult Receives true when granted, false when denied.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MusicPermissionRequest(
    requestTrigger: Int,
    onPermissionResult: (Boolean) -> Unit = {},
) {
    val context = LocalContext.current
    val latestOnPermissionResult by rememberUpdatedState(newValue = onPermissionResult)
    var hasRequested by rememberSaveable { mutableStateOf(false) }
    var deniedAnswerCount by rememberSaveable { mutableIntStateOf(0) }
    // Plain remember on purpose: a fresh view starts from the trigger it was given, so an
    // old tap left in the Fragment is never replayed.
    var lastHandledTrigger by remember { mutableIntStateOf(requestTrigger) }
    val permissionState = rememberPermissionState(
        permission = audioPermissionFor(sdkInt = Build.VERSION.SDK_INT),
        onPermissionResult = { granted ->
            if (granted) return@rememberPermissionState
            deniedAnswerCount++
        },
    )

    // Ask once automatically when the screen opens for the first time.
    LaunchedEffect(Unit) {
        if (permissionState.status.isGranted) return@LaunchedEffect
        if (hasRequested) return@LaunchedEffect
        hasRequested = true
        permissionState.launchPermissionRequest()
    }

    // Report every change of the answer (dialog result or re-check on resume).
    LaunchedEffect(permissionState.status) {
        latestOnPermissionResult(permissionState.status.isGranted)
    }

    // The user tapped the grant button (only taps made while this view is on screen count).
    LaunchedEffect(requestTrigger) {
        if (requestTrigger <= lastHandledTrigger) return@LaunchedEffect
        lastHandledTrigger = requestTrigger
        hasRequested = true
        performGrantPermissionTap(
            context = context,
            permissionState = permissionState,
            deniedAnswerCount = deniedAnswerCount,
            onGranted = {
                latestOnPermissionResult(true)
            },
        )
    }
}

/**
 * Does what one tap on "Grant permission" should do (see the story above).
 *
 * @param deniedAnswerCount How many requests came back "not granted" so far.
 * @param onGranted Called when the permission turns out to be on already.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalPermissionsApi::class)
private fun performGrantPermissionTap(
    context: Context,
    permissionState: PermissionState,
    deniedAnswerCount: Int,
    onGranted: () -> Unit,
) {
    val action = grantPermissionTapAction(
        isGranted = permissionState.status.isGranted,
        shouldShowRationale = permissionState.status.shouldShowRationale,
        deniedAnswerCount = deniedAnswerCount,
    )
    when (action) {
        GrantPermissionTapAction.ReportGranted -> onGranted()
        GrantPermissionTapAction.ShowSystemDialog -> permissionState.launchPermissionRequest()
        GrantPermissionTapAction.OpenSettings -> openAppDetailsSettings(context = context)
    }
}

/**
 * Opens this app's page in the phone Settings so the user can switch the permission on.
 *
 * @author Phong-Kaster
 */
private fun openAppDetailsSettings(context: Context) {
    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
        data = Uri.fromParts("package", context.packageName, null)
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e(TAG, "openAppDetailsSettings() failed", e)
    }
}

/**
 * Body shown when the audio permission is missing: a short message and a grant button.
 *
 * Example:
 * ```kotlin
 * MusicPermissionDenied(onGrantPermission = { triggerRequestPermission++ })
 * ```
 *
 * @param modifier Outer modifier.
 * @param onGrantPermission Called when the user taps the grant button.
 * @author Phong-Kaster
 */
@Composable
fun MusicPermissionDenied(
    modifier: Modifier = Modifier,
    onGrantPermission: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        content = {
            Text(
                text = stringResource(R.string.allow_audio_access_to_list_songs),
                style = customizedTextStyle(
                    fontSize = 16,
                    fontWeight = 500,
                    lineHeight = 22,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                textAlign = TextAlign.Center,
            )

            Button(
                onClick = onGrantPermission,
                content = {
                    Text(
                        text = stringResource(R.string.grant_permission),
                        style = customizedTextStyle(
                            fontSize = 14,
                            fontWeight = 600,
                            lineHeight = 20,
                            color = MaterialTheme.colorScheme.onPrimary,
                        ),
                    )
                },
            )
        },
    )
}

@Preview
@Composable
private fun MusicPermissionDeniedPreview() {
    MusicPermissionDenied()
}
