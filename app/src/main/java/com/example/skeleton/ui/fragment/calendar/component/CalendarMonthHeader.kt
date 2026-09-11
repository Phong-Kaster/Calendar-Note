package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * The bar above the grid: back a month, the month's name, forward a month.
 *
 * [label] arrives already formatted rather than as a `YearMonth`, for two reasons. Turning a month
 * into words is a locale question, and the answer belongs where the app's locale is known — the
 * screen — not in a component that would then be untestable by picture on any machine whose JDK
 * disagrees. It also means a reference image of this row can be recorded against a fixed string.
 *
 * The arrows are auto-mirrored Material icons rather than "<" and ">" characters: as glyphs they
 * are small targets, they carry no tint of their own, and they keep pointing the same way under a
 * right-to-left locale.
 *
 * @param label the month in words, e.g. "September 2026".
 * @param onPrevious the user wants the month before.
 * @param onNext the user wants the month after.
 * @param modifier applied to the row.
 * @author Phong-Kaster
 */
@Composable
fun CalendarMonthHeader(
    label: String,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(
            onClick = onPrevious,
            content = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.previous_month),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp),
                )
            },
        )

        Text(
            text = label,
            style = customizedTextStyle(fontSize = 18, fontWeight = 600),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        IconButton(
            onClick = onNext,
            content = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.next_month),
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(28.dp),
                )
            },
        )
    }
}

@Preview(name = "Month header", widthDp = 360, heightDp = 72)
@Composable
private fun CalendarMonthHeaderPreview() {
    MyApplicationTheme {
        CalendarMonthHeader(label = "September 2026")
    }
}
