package com.example.skeleton.ui.fragment.music.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Explains why the app needs to read music and offers one button.
 * - [shouldOpenSettings] false → "Allow access" asks the system permission dialog ([onRequestPermission]).
 * - [shouldOpenSettings] true → the user said "don't ask again", so "Open settings" opens app settings ([onOpenSettings]).
 *
 * Example: `MusicPermissionNotice(shouldOpenSettings = false, onRequestPermission = { launcher.launch(permission) })`
 * @param shouldOpenSettings True when the system dialog can no longer be shown.
 * @param modifier Outer modifier.
 * @param onRequestPermission Called to show the system permission dialog.
 * @param onOpenSettings Called to open this app's settings page.
 * @author Phong-Kaster
 */
@Composable
fun MusicPermissionNotice(
    shouldOpenSettings: Boolean,
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val buttonText = if (shouldOpenSettings) {
        stringResource(R.string.open_settings)
    } else {
        stringResource(R.string.allow_access)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(R.string.allow_access_to_your_music_to_play_songs),
            style = customizedTextStyle(
                fontSize = 16,
                fontWeight = 400,
                lineHeight = 24,
                color = MaterialTheme.colorScheme.onSurface,
            ),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = {
                if (shouldOpenSettings) {
                    onOpenSettings()
                } else {
                    onRequestPermission()
                }
            },
            content = {
                Text(
                    text = buttonText,
                    style = customizedTextStyle(
                        fontSize = 14,
                        fontWeight = 600,
                        lineHeight = 20,
                        color = MaterialTheme.colorScheme.onPrimary,
                    ),
                )
            },
        )
    }
}

@Preview(name = "Allow access")
@Composable
private fun MusicPermissionNoticeRequestPreview() {
    MyApplicationTheme(
        content = {
            MusicPermissionNotice(shouldOpenSettings = false)
        }
    )
}

@Preview(name = "Open settings")
@Composable
private fun MusicPermissionNoticeSettingsPreview() {
    MyApplicationTheme(
        content = {
            MusicPermissionNotice(shouldOpenSettings = true)
        }
    )
}
