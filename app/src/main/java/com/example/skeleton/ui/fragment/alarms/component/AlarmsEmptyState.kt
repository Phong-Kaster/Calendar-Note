package com.example.skeleton.ui.fragment.alarms.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * What the user sees on the Alarms screen before there is a single alarm on it.
 *
 * A blank content area reads as "this failed to load", not as "you have not made one yet" — so the
 * sentence is the whole component. Centred in the entire content area rather than tucked under the
 * top bar, for the same reason the Home screen's empty message is: one stray line near the top
 * looks like a bug, a centred line looks like a state.
 *
 * Styled to match `HomeNoteList`'s empty message exactly, on purpose. Two top-level screens saying
 * "there is nothing here" in two different sizes reads as two different kinds of nothing.
 *
 * @param modifier applied to the centring box.
 * @author Phong-Kaster
 */
@Composable
fun AlarmsEmptyState(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_alarms_yet),
            style = customizedTextStyle(
                fontSize = 16,
                fontWeight = 500,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Alarms empty state", widthDp = 360, heightDp = 420)
@Composable
private fun AlarmsEmptyStatePreview() {
    // On the real ground colour, not on Studio's default white. This component is nothing but a
    // line of text tinted from the theme, so a preview on the wrong background proves nothing
    // about whether the user can read it — the same reasoning as `CoreBottomBar`'s preview.
    MyApplicationTheme(
        content = {
            Box(modifier = Modifier.background(color = MaterialTheme.colorScheme.background)) {
                AlarmsEmptyState()
            }
        },
    )
}
