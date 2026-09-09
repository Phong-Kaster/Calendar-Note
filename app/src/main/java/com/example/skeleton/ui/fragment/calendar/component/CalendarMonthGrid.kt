package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.CalendarMonth
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalDate
import java.time.YearMonth

/**
 * Renders a weekday header row above [days] laid out as a 7-column grid of [CalendarDayCell]s.
 *
 * The header order is **Sunday-first**, matching [CalendarMonth.buildGrid]'s leading-blank
 * arithmetic. These two must not drift apart: a header that disagrees with the grid is worse than
 * no header, because it looks authoritative while pointing at the wrong column.
 *
 * Laid out with plain `Column`/`Row` rather than `LazyVerticalGrid`. A month is at most 42 cells,
 * all of them visible, so laziness buys nothing — and a lazy vertical grid nested inside the
 * screen's `verticalScroll` throws at measure time for having an infinite height constraint.
 *
 * @param days one entry per grid cell; null entries are leading/trailing padding.
 * @param today real-world today, used to highlight the matching cell.
 * @param selectedDate the date whose notes are shown, drawn with a ring; null when unselected.
 * @param datesWithNotes every date that should render the "has notes" marker.
 * @param onDayClick called with the tapped date; threaded through to every [CalendarDayCell].
 * @author Phong-Kaster
 */
@Composable
fun CalendarMonthGrid(
    days: List<LocalDate?>,
    today: LocalDate,
    selectedDate: LocalDate? = null,
    datesWithNotes: Set<LocalDate> = emptySet(),
    onDayClick: (LocalDate) -> Unit = {},
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        WeekdayHeaderRow()

        days.chunked(DAYS_IN_WEEK).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { date ->
                    Column(modifier = Modifier.weight(1f)) {
                        CalendarDayCell(
                            date = date,
                            isToday = date == today,
                            isSelected = date != null && date == selectedDate,
                            hasNotes = date != null && date in datesWithNotes,
                            onClick = onDayClick,
                        )
                    }
                }

                // A final partial week would otherwise stretch its cells across the full width.
                repeat(DAYS_IN_WEEK - week.size) {
                    Column(modifier = Modifier.weight(1f)) {}
                }
            }
        }
    }
}

/** Sunday-first weekday labels, one per grid column. */
@Composable
private fun WeekdayHeaderRow() {
    val labels = listOf(
        stringResource(R.string.weekday_short_sun),
        stringResource(R.string.weekday_short_mon),
        stringResource(R.string.weekday_short_tue),
        stringResource(R.string.weekday_short_wed),
        stringResource(R.string.weekday_short_thu),
        stringResource(R.string.weekday_short_fri),
        stringResource(R.string.weekday_short_sat),
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
    ) {
        labels.forEach { label ->
            Text(
                text = label,
                style = customizedTextStyle(
                    fontSize = 12,
                    fontWeight = 600,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .height(20.dp),
            )
        }
    }
}

private const val DAYS_IN_WEEK = 7

@Preview
@Composable
private fun CalendarMonthGridPreview() {
    val today = LocalDate.of(2026, 9, 8)
    CalendarMonthGrid(
        days = CalendarMonth(YearMonth.of(2026, 9)).buildGrid(),
        today = today,
        selectedDate = LocalDate.of(2026, 9, 15),
        datesWithNotes = setOf(LocalDate.of(2026, 9, 8), LocalDate.of(2026, 9, 20)),
    )
}
