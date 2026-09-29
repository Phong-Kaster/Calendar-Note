package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
 * Shown when the screen opens but no song is loaded (for example from a deep link).
 * The top bar's back button stays visible, so the user can leave.
 *
 * Example:
 * ```kotlin
 * if (uiState.hasSong.not()) NowPlayingEmptyState()
 * ```
 *
 * @param modifier extra layout tweaks.
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingEmptyState(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
    ) {
        SongArtwork(size = 96.dp, cornerRadius = 16.dp)

        Text(
            text = stringResource(R.string.nothing_playing),
            style = customizedTextStyle(
                fontSize = 18,
                fontWeight = 600,
                lineHeight = 26,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun NowPlayingEmptyStatePreview() {
    MyApplicationTheme(
        content = {
            NowPlayingEmptyState()
        }
    )
}
