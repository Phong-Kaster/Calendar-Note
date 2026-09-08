package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalDate

/**
 * Renders one cell of the month grid: a blank box when [date] is null (padding cell),
 * otherwise the day-of-month number, highlighted when [isToday] is true.
 *
 * @param date the day this cell represents; null renders an empty padding cell.
 * @param isToday true when [date] equals the real-world today.
 * @param hasNotes reserved for a later task (shows a note indicator dot); unused visually yet.
 * @param onClick called with [date] when the cell is tapped; never called for a null [date].
 * @author Phong-Kaster
 */
@Composable
fun CalendarDayCell(
    date: LocalDate?,
    isToday: Boolean,
    hasNotes: Boolean = false,
    onClick: (LocalDate) -> Unit = {},
) {
    if (date == null) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
        return
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(date) },
            ),
        contentAlignment = Alignment.Center,
    ) {
        val backgroundModifier = if (isToday) {
            Modifier.background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
        } else {
            Modifier
        }

        Box(
            modifier = Modifier.fillMaxWidth().aspectRatio(1f).then(backgroundModifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = if (isToday) 700 else 400,
                    color = Color.White,
                ),
            )
        }
    }
}

@Preview(name = "Regular day")
@Composable
private fun CalendarDayCellPreview() {
    CalendarDayCell(date = LocalDate.of(2026, 9, 8), isToday = false)
}

@Preview(name = "Today")
@Composable
private fun CalendarDayCellTodayPreview() {
    CalendarDayCell(date = LocalDate.of(2026, 9, 8), isToday = true)
}
