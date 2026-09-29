package com.example.skeleton.ui.fragment.library.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.skeleton.ui.component.SongArtwork
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Shown when we are NOT allowed to read music. It explains why we need access and shows one button:
 * - normally the button says "Allow" and shows the system dialog again;
 * - after "permanently denied" it says "Open settings", because the system will not ask again.
 *
 * Example:
 * ```kotlin
 * LibraryPermissionRequired(
 *     isPermanentlyDenied = uiState.isPermissionPermanentlyDenied,
 *     onRequestPermission = { permissionState.launchPermissionRequest() },
 *     onOpenSettings = { openAppSettings() },
 * )
 * ```
 *
 * @param isPermanentlyDenied true when only app Settings can grant the permission now.
 * @param modifier extra layout tweaks.
 * @param onRequestPermission shows the system permission dialog.
 * @param onOpenSettings opens this app's page in system Settings.
 * @author Phong-Kaster
 */
@Composable
fun LibraryPermissionRequired(
    isPermanentlyDenied: Boolean,
    modifier: Modifier = Modifier,
    onRequestPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
) {
    val messageRes = if (isPermanentlyDenied) R.string.music_access_turned_off else R.string.allow_access_to_your_music
    val buttonRes = if (isPermanentlyDenied) R.string.open_settings else R.string.allow
    val onButtonClick = if (isPermanentlyDenied) onOpenSettings else onRequestPermission

    Column(
        modifier = modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SongArtwork(size = 96.dp, cornerRadius = 24.dp)
        Text(
            text = stringResource(messageRes),
            style = customizedTextStyle(
                fontSize = 16,
                fontWeight = 400,
                lineHeight = 22,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onButtonClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
            content = {
                Text(
                    text = stringResource(buttonRes),
                    style = customizedTextStyle(
                        fontSize = 15,
                        fontWeight = 600,
                        lineHeight = 20,
                        color = MaterialTheme.colorScheme.onPrimary,
                    ),
                    maxLines = 1,
                )
            },
        )
    }
}

@Preview(name = "Denied", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun LibraryPermissionRequiredPreview() {
    MyApplicationTheme(
        content = {
            LibraryPermissionRequired(isPermanentlyDenied = false)
        }
    )
}

@Preview(name = "Permanently denied", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun LibraryPermissionRequiredPermanentPreview() {
    MyApplicationTheme(
        content = {
            LibraryPermissionRequired(isPermanentlyDenied = true)
        }
    )
}
