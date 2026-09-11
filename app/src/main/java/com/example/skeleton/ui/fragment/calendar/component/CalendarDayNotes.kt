package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.component.NoteSummaryRow
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalDate

/*
 * --- Why this section always draws something (simple story) ---
 *
 * There are three things that can be under the grid, and only one of them has notes in it:
 *
 *   1. a day is picked and it has notes  → the heading, then the notes;
 *   2. a day is picked and it has none   → the heading, then "nothing written on this day yet";
 *   3. no day is picked at all           → "pick a day to see what is on it".
 *
 * The mistake this component exists to prevent is drawing **nothing** for case 2. A prior attempt
 * at this screen did exactly that, and the result is not a neutral empty space: the grid stays
 * put, the square the user just tapped lights up, and then there is a gap. That reads as a list
 * that failed to load, so the user taps the day again — and gets the same gap, because nothing
 * was wrong in the first place.
 *
 * Case 3 is not theoretical either, though it takes an odd route to get there. The screen opens
 * with today picked and `selectDate` never clears a selection — but `refreshToday` drops one that
 * a *backwards* clock has left in the future (fly west, correct the date by hand, take an NTP
 * step back), so "nothing picked" is a state this section can be handed.
 */

/**
 * The notes written on the day picked in the grid above, or a message saying why there are none.
 *
 * **[dayLabel] arrives already written out, as a plain `String`.** This composable does no date
 * formatting, and that is deliberate rather than lazy: `DateTimeFormatter.ofLocalizedDate(...)`
 * produces different words per locale, so a component that formatted its own date could not be
 * recorded as a reference image without pinning the machine's locale into the picture. The screen
 * above formats it against the *app's* configured language (which is not always the device's —
 * this app has its own picker in Settings), and a screenshot case passes a fixed string.
 *
 * **A plain [Column], not a `LazyColumn`, and not by oversight.** This section lives inside the
 * Calendar screen's single vertical scroll, so the whole page — grid and notes together — moves as
 * one; a lazy list nested in a scrolling parent throws at measure time for being handed an
 * infinite height. The trade is that every row here is composed rather than only the visible ones,
 * which is affordable because this is **one day's** notes rather than Home's every-note list.
 *
 * @param dayLabel the picked day, already formatted for display, or null when no day is picked.
 * @param notes that day's notes, most recently touched first. Already ordered by the store.
 * @param onOpenNote the user tapped a note and wants it opened. **No default**, like
 *   `HomeNoteList.onOpenNote`: a row shows a truncated note, so a tap that goes nowhere leaves the
 *   rest of the text unreachable while looking like a working screen.
 * @param modifier applied to the whole section.
 * @author Phong-Kaster
 */
@Composable
fun CalendarDayNotes(
    dayLabel: String?,
    notes: List<Note>,
    onOpenNote: (Note) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        HorizontalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant,
        )

        if (dayLabel == null) {
            CalendarDayNotesMessage(text = stringResource(R.string.pick_a_day_to_see_its_notes))
            return@Column
        }

        // Two lines rather than the house default of one-line-plus-marquee, and the honest
        // version of why: **no shipped locale currently overflows this line.** Both English and
        // German fit on one, measured at 288dp — a 320dp screen's content width — by the
        // reference image `DayNotesGermanHeading`. So this is insurance, not a fix.
        //
        // It is worth taking because of what the line *is*: the only thing naming the day the
        // notes below belong to. Ellipsised it would silently eat the year, which is the part a
        // reader came for; a marquee would set the heading of a page somebody is reading in
        // motion. A longer locale or a large accessibility font scale is enough to reach that,
        // and wrapping costs one line of height on a page that already scrolls freely.
        Text(
            text = dayLabel,
            style = customizedTextStyle(fontSize = 16, fontWeight = 700),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        if (notes.isEmpty()) {
            CalendarDayNotesMessage(text = stringResource(R.string.nothing_written_on_this_day_yet))
            return@Column
        }

        notes.forEach { note ->
            NoteSummaryRow(
                note = note,
                onClick = { onOpenNote(note) },
                // Every row here belongs to the day named above, so printing that same date on
                // each card would repeat the heading once per note and push the body text down.
                showDate = false,
            )
        }
    }
}

/**
 * One line of explanation where a list would otherwise be.
 *
 * Given room to breathe and centred, so it reads as "this is the answer" rather than as a stray
 * label that failed to load — which is precisely the reading a bare blank space gets.
 *
 * @param text what to say.
 * @param modifier applied to the centring box.
 * @author Phong-Kaster
 */
@Composable
private fun CalendarDayNotesMessage(
    text: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = customizedTextStyle(
                fontSize = 14,
                fontWeight = 500,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

@Preview(name = "Day notes - with notes", widthDp = 360)
@Composable
private fun CalendarDayNotesPreview() {
    MyApplicationTheme {
        CalendarDayNotes(
            dayLabel = "Thursday, 10 September 2026",
            notes = listOf(
                Note(
                    id = 1L,
                    date = LocalDate.of(2026, 9, 10),
                    title = "Groceries",
                    content = "Coffee, oat milk, the good bread from the corner shop.",
                    createdAt = 1_773_000_000_000L,
                    updatedAt = 1_773_000_000_000L,
                ),
                Note(
                    id = 2L,
                    date = LocalDate.of(2026, 9, 10),
                    title = "",
                    content = "No title on this one, so the first line of the body becomes the heading.",
                    createdAt = 1_772_900_000_000L,
                    updatedAt = 1_772_900_000_000L,
                ),
            ),
            onOpenNote = {},
        )
    }
}

@Preview(name = "Day notes - empty day", widthDp = 360)
@Composable
private fun CalendarDayNotesEmptyPreview() {
    MyApplicationTheme {
        CalendarDayNotes(
            dayLabel = "Thursday, 3 September 2026",
            notes = emptyList(),
            onOpenNote = {},
        )
    }
}

@Preview(name = "Day notes - nothing picked", widthDp = 360)
@Composable
private fun CalendarDayNotesNoSelectionPreview() {
    MyApplicationTheme {
        CalendarDayNotes(
            dayLabel = null,
            notes = emptyList(),
            onOpenNote = {},
        )
    }
}
