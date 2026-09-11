package com.example.skeleton.ui.fragment.calendar.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.CalendarMonth
import com.example.skeleton.ui.fragment.calendar.model.DayCellState
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * The month page: a row of weekday labels, then the days laid out seven to a row.
 *
 * **The labels and the squares get their week start from the same place**,
 * [CalendarMonth.WEEK_START], so they cannot drift apart. They used to be two separate
 * Sunday-first lists, which is worse than no header at all: a header that disagrees with the
 * grid below it keeps looking authoritative while pointing at the wrong column, and no unit test
 * can see it. The full-grid reference image in `CalendarScreenshotTest.kt` is the backstop.
 *
 * Plain `Column`/`Row`, not `LazyVerticalGrid`: a month is at most 42 squares and every one of
 * them is on screen, so laziness buys nothing — and a lazy grid nested in a scrolling parent
 * throws at measure time for being handed an infinite height.
 *
 * @param days one entry per square, from [CalendarMonth.buildGrid]; null entries are padding.
 * @param today the real-world today.
 * @param displayedMonth the month these squares belong to.
 * @param selectedDate the day whose notes are being shown, drawn with a ring; null when nothing
 *   is picked. **Thread this through.** A prior attempt at this screen computed a selected date
 *   and never passed it down, so selection was invisible while every test passed.
 * @param datesWithNotes every day that should show the has-notes dot.
 * @param onDayClick the user tapped a day. Only ever called for a day that is selectable.
 * @param modifier applied to the whole page.
 * @author Phong-Kaster
 */
@Composable
fun CalendarMonthGrid(
    days: List<LocalDate?>,
    today: LocalDate,
    displayedMonth: YearMonth,
    selectedDate: LocalDate? = null,
    datesWithNotes: Set<LocalDate> = emptySet(),
    onDayClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        WeekdayHeaderRow()

        // `buildGrid` pads to a whole number of weeks, so every chunk here is seven long and no
        // final row needs filling out by hand.
        days.chunked(size = CalendarMonth.DAYS_IN_WEEK).forEach { week ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                week.forEach { date ->
                    CalendarDayCell(
                        date = date,
                        state = DayCellState.of(
                            date = date,
                            today = today,
                            displayedMonth = displayedMonth,
                        ),
                        isSelected = date != null && date == selectedDate,
                        hasNotes = date != null && date in datesWithNotes,
                        onClick = onDayClick,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * The seven column labels, in whatever order [CalendarMonth.WEEK_START] puts them.
 *
 * **The order is asked for, not written down here.** A hardcoded Sunday-to-Saturday list beside
 * a grid that computes its own offset is two copies of one decision, and the day somebody
 * changes the week start, only one of them moves — leaving a header that still looks
 * authoritative while labelling the wrong columns.
 *
 * The words themselves are fixed strings from `strings.xml`, not
 * `DayOfWeek.getDisplayName(...)`, and that is not laziness. `getDisplayName` follows the
 * *JVM's* default locale, which on a rendering host is whatever that machine happens to be set
 * to — so a reference image of this row would pass here and fail on a colleague's laptop for a
 * reason that has nothing to do with the app. A `stringResource` follows the app's own locale,
 * which the screenshot harness pins.
 *
 * @author Phong-Kaster
 */
@Composable
private fun WeekdayHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        CalendarMonth.weekdaysInGridOrder().forEach { dayOfWeek ->
            Text(
                text = stringResource(weekdayLabelOf(dayOfWeek = dayOfWeek)),
                style = customizedTextStyle(
                    fontSize = 12,
                    fontWeight = 600,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

/**
 * The short label for one day of the week.
 *
 * A `when` over the enum rather than a map, so that the compiler — not a runtime lookup that
 * could return null — is what guarantees all seven have a word.
 *
 * @param dayOfWeek the day to name.
 * @return the string resource holding its three-letter form.
 * @author Phong-Kaster
 */
@StringRes
private fun weekdayLabelOf(dayOfWeek: DayOfWeek): Int = when (dayOfWeek) {
    DayOfWeek.SUNDAY -> R.string.weekday_short_sun
    DayOfWeek.MONDAY -> R.string.weekday_short_mon
    DayOfWeek.TUESDAY -> R.string.weekday_short_tue
    DayOfWeek.WEDNESDAY -> R.string.weekday_short_wed
    DayOfWeek.THURSDAY -> R.string.weekday_short_thu
    DayOfWeek.FRIDAY -> R.string.weekday_short_fri
    DayOfWeek.SATURDAY -> R.string.weekday_short_sat
}

@Preview(name = "Month grid", widthDp = 360, heightDp = 400)
@Composable
private fun CalendarMonthGridPreview() {
    MyApplicationTheme {
        CalendarMonthGrid(
            days = CalendarMonth(yearMonth = YearMonth.of(2026, 9)).buildGrid(),
            today = LocalDate.of(2026, 9, 10),
            displayedMonth = YearMonth.of(2026, 9),
            selectedDate = LocalDate.of(2026, 9, 3),
            datesWithNotes = setOf(LocalDate.of(2026, 9, 10), LocalDate.of(2026, 9, 3)),
        )
    }
}
