package com.example.skeleton.ui.fragment.music.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Message shown when the permission is granted but the phone has no songs.
 *
 * Example:
 * ```kotlin
 * MusicEmptyState(modifier = Modifier.fillMaxSize())
 * ```
 *
 * @param modifier Outer modifier.
 * @author Phong-Kaster
 */
@Composable
fun MusicEmptyState(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.no_songs_found),
        style = customizedTextStyle(
            fontSize = 16,
            fontWeight = 500,
            lineHeight = 22,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        textAlign = TextAlign.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp),
    )
}

@Preview
@Composable
private fun MusicEmptyStatePreview() {
    MusicEmptyState()
}
