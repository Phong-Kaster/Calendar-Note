package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Month header row: previous arrow, "Month Year" label, next arrow.
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
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        val previousDescription = stringResource(R.string.previous_month)
        IconButton(
            onClick = onPrevious,
            modifier = Modifier.semantics { contentDescription = previousDescription },
        ) {
            Text(
                text = "<",
                style = customizedTextStyle(fontSize = 20, fontWeight = 600, color = Color.White),
            )
        }

        Text(
            text = label,
            style = customizedTextStyle(fontSize = 18, fontWeight = 600, color = Color.White),
        )

        val nextDescription = stringResource(R.string.next_month)
        IconButton(
            onClick = onNext,
            modifier = Modifier.semantics { contentDescription = nextDescription },
        ) {
            Text(
                text = ">",
                style = customizedTextStyle(fontSize = 20, fontWeight = 600, color = Color.White),
            )
        }
    }
}

@Preview
@Composable
private fun CalendarMonthHeaderPreview() {
    CalendarMonthHeader(label = "September 2026")
}
