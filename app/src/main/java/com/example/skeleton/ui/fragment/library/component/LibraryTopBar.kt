package com.example.skeleton.ui.fragment.library.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Title bar of the Library screen. The app draws behind the status bar (edge-to-edge),
 * so this bar pushes itself down with `statusBarsPadding()` to stay below the clock/battery icons.
 *
 * Example:
 * ```kotlin
 * CoreLayout(topBar = { LibraryTopBar() }, content = { ... })
 * ```
 *
 * @param modifier extra layout tweaks.
 * @author Phong-Kaster
 */
@Composable
fun LibraryTopBar(
    modifier: Modifier = Modifier,
) {
    Text(
        text = stringResource(R.string.library),
        style = customizedTextStyle(
            fontSize = 24,
            fontWeight = 700,
            lineHeight = 32,
            color = MaterialTheme.colorScheme.onBackground,
        ),
        maxLines = 1,
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .basicMarquee(iterations = Int.MAX_VALUE),
    )
}

@Preview
@Composable
private fun LibraryTopBarPreview() {
    MyApplicationTheme(
        content = {
            LibraryTopBar()
        }
    )
}
