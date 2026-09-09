package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalDate

/**
 * Renders one cell of the month grid: a blank box when [date] is null (padding cell),
 * otherwise the day-of-month number with up to three independent markers.
 *
 * The three states stack rather than compete, which is the reason the colours are picked the way
 * they are:
 *
 * - **today** fills the cell with `primary`, so anything drawn on top of it must use `onPrimary`.
 *   The day number and the has-notes dot both do. (Using `primary` for the dot as well, as an
 *   earlier version did, made the dot invisible on exactly the day the user looks at first.)
 * - **selected** draws a `primary` ring instead of a fill, so a day can be today *and* selected
 *   and still read as both.
 * - **has notes** is a dot under the number, always contrasting with whatever is behind it.
 *
 * @param date the day this cell represents; null renders an empty padding cell.
 * @param isToday true when [date] equals the real-world today.
 * @param isSelected true when [date] is the date whose notes are being shown.
 * @param hasNotes shows a small dot below the day number when true.
 * @param onClick called with [date] when the cell is tapped; never called for a null [date].
 * @author Phong-Kaster
 */
@Composable
fun CalendarDayCell(
    date: LocalDate?,
    isToday: Boolean,
    isSelected: Boolean = false,
    hasNotes: Boolean = false,
    onClick: (LocalDate) -> Unit = {},
) {
    if (date == null) {
        Box(modifier = Modifier.fillMaxWidth().aspectRatio(1f))
        return
    }

    val shapeModifier = when {
        isToday -> Modifier.background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
        isSelected -> Modifier.border(width = 2.dp, color = MaterialTheme.colorScheme.primary, shape = CircleShape)
        else -> Modifier
    }

    // On a primary-filled cell the foreground has to be onPrimary; everywhere else it sits on the
    // app's black ground and uses onBackground.
    val foregroundColor = if (isToday) {
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onBackground
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { onClick(date) },
            )
            .then(shapeModifier),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = if (isToday || isSelected) 700 else 400,
                    color = foregroundColor,
                ),
            )

            // The row is always reserved, so day numbers don't jump by a few pixels as notes
            // are added and removed.
            Box(
                modifier = Modifier.height(6.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (hasNotes) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(color = foregroundColor, shape = CircleShape),
                    )
                }
            }
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

@Preview(name = "Has notes")
@Composable
private fun CalendarDayCellHasNotesPreview() {
    CalendarDayCell(date = LocalDate.of(2026, 9, 8), isToday = false, hasNotes = true)
}

@Preview(name = "Today with notes")
@Composable
private fun CalendarDayCellTodayWithNotesPreview() {
    CalendarDayCell(date = LocalDate.of(2026, 9, 8), isToday = true, hasNotes = true)
}

@Preview(name = "Selected")
@Composable
private fun CalendarDayCellSelectedPreview() {
    CalendarDayCell(date = LocalDate.of(2026, 9, 8), isToday = false, isSelected = true)
}

@Preview(name = "Selected with notes")
@Composable
private fun CalendarDayCellSelectedWithNotesPreview() {
    CalendarDayCell(date = LocalDate.of(2026, 9, 8), isToday = false, isSelected = true, hasNotes = true)
}
