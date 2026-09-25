package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.component.dynamicStatusBarPadding
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Top bar of the Now Playing screen: a back button and the "Now playing" title.
 * It is drawn here (not with CoreTopBar) so every colour comes from the theme.
 *
 * Example:
 * ```kotlin
 * NowPlayingTopBar(onBackClick = { safeNavigateUp() })
 * ```
 *
 * @param onBackClick Called when the user taps back.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingTopBar(
    onBackClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        content = {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .dynamicStatusBarPadding(),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                content = {
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.size(48.dp),
                        content = {
                            Icon(
                                painter = painterResource(R.drawable.ic_back),
                                contentDescription = stringResource(R.string.back),
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                    )

                    Text(
                        text = stringResource(R.string.now_playing),
                        style = customizedTextStyle(
                            fontSize = 18,
                            fontWeight = 600,
                            lineHeight = 24,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        maxLines = 1,
                        modifier = Modifier
                            .weight(1f)
                            .basicMarquee(iterations = Int.MAX_VALUE),
                    )
                },
            )
        },
    )
}

@Preview
@Composable
private fun NowPlayingTopBarPreview() {
    NowPlayingTopBar()
}
