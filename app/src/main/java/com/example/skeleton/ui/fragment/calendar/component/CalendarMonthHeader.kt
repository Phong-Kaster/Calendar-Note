package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Month header row: previous arrow, "Month Year" label, next arrow.
 *
 * The arrows are auto-mirrored Material icons rather than "<" and ">" text: as glyphs they were
 * hard to hit accurately, carried no tint of their own, and stayed pointing the same way under a
 * right-to-left locale, which this app ships translations for.
 *
 * @param label display text such as "September 2026".
 * @param onPrevious called when the user taps the left arrow.
 * @param onNext called when the user taps the right arrow.
 * @author Phong-Kaster
 */
@Composable
fun CalendarMonthHeader(
    label: String,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.previous_month),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }

        Text(
            text = label,
            style = customizedTextStyle(
                fontSize = 18,
                fontWeight = 600,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )

        IconButton(onClick = onNext) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = stringResource(R.string.next_month),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Preview
@Composable
private fun CalendarMonthHeaderPreview() {
    CalendarMonthHeader(label = "September 2026")
}
