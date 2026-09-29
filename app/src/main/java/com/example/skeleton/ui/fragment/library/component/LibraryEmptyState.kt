package com.example.skeleton.ui.fragment.library.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
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
 * Shown when we are allowed to read music but the phone has no songs.
 * A big music-note tile plus a short message in the middle of the screen.
 *
 * Example:
 * ```kotlin
 * if (uiState.showEmptyState) LibraryEmptyState()
 * ```
 *
 * @param modifier extra layout tweaks.
 * @author Phong-Kaster
 */
@Composable
fun LibraryEmptyState(
    modifier: Modifier = Modifier,
) {
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
            text = stringResource(R.string.no_songs_found),
            style = customizedTextStyle(
                fontSize = 16,
                fontWeight = 500,
                lineHeight = 22,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun LibraryEmptyStatePreview() {
    MyApplicationTheme(
        content = {
            LibraryEmptyState()
        }
    )
}
