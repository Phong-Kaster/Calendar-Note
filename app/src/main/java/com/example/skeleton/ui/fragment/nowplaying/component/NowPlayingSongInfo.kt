package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
 * The big cover placeholder with the song title and artist under it.
 * Long names scroll slowly (marquee) instead of wrapping.
 *
 * Example:
 * ```kotlin
 * NowPlayingSongInfo(title = "Blue Sky", artist = "The Band", isArtistUnknown = false)
 * ```
 *
 * @param title song name.
 * @param artist artist name (ignored when [isArtistUnknown] is true).
 * @param isArtistUnknown true shows "Unknown artist" instead of [artist].
 * @param modifier extra layout tweaks.
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingSongInfo(
    title: String,
    artist: String,
    isArtistUnknown: Boolean,
    modifier: Modifier = Modifier,
) {
    val artistText = if (isArtistUnknown) stringResource(R.string.unknown_artist) else artist

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        SongArtwork(size = 280.dp, cornerRadius = 24.dp)

        Text(
            text = title,
            style = customizedTextStyle(
                fontSize = 22,
                fontWeight = 700,
                lineHeight = 30,
                color = MaterialTheme.colorScheme.onBackground,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier
                .padding(top = 24.dp)
                .basicMarquee(iterations = Int.MAX_VALUE),
        )
        Text(
            text = artistText,
            style = customizedTextStyle(
                fontSize = 16,
                fontWeight = 400,
                lineHeight = 22,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
        )
    }
}

@Preview(name = "Known artist", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun NowPlayingSongInfoPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingSongInfo(
                title = "A very long song title that should scroll instead of wrapping",
                artist = "The Band",
                isArtistUnknown = false,
            )
        }
    )
}

@Preview(name = "Unknown artist", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun NowPlayingSongInfoUnknownArtistPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingSongInfo(
                title = "Voice memo",
                artist = "<unknown>",
                isArtistUnknown = true,
            )
        }
    )
}
