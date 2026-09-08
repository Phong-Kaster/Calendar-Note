package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.skeleton.domain.model.CalendarMonth
import java.time.LocalDate

/**
 * Renders [days] as a 7-column grid of [CalendarDayCell]s.
 *
 * @param days one entry per grid cell; null entries are leading/trailing padding.
 * @param today real-world today, used to highlight the matching cell.
 * @param onDayClick called with the tapped date; threaded through to every [CalendarDayCell]
 * so a later task can react to day taps without changing this grid.
 * @author Phong-Kaster
 */
@Composable
fun CalendarMonthGrid(
    days: List<LocalDate?>,
    today: LocalDate,
    onDayClick: (LocalDate) -> Unit = {},
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier.fillMaxWidth(),
    ) {
        items(items = days) { date ->
            CalendarDayCell(
                date = date,
                isToday = date == today,
                hasNotes = false,
                onClick = onDayClick,
            )
        }
    }
}

@Preview
@Composable
private fun CalendarMonthGridPreview() {
    val today = LocalDate.of(2026, 9, 8)
    CalendarMonthGrid(
        days = CalendarMonth.current().buildGrid(),
        today = today,
    )
}
