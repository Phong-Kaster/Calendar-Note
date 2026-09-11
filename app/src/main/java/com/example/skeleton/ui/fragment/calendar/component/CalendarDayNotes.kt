package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
 *
 * --- Why the add action lives in here, past the first return ---
 *
 * Cases 1 and 2 offer "add a note to this day"; case 3 does not. That is not a cosmetic choice:
 * with no day picked there is no day to file a note under, and the only other answers available
 * are both worse. Filing it under **today** would put a note on a day the user did not choose,
 * quietly. Drawing the action **disabled** would invite them to work out what they did wrong,
 * when the answer is only "pick a day first" — which is what case 3's message already says.
 *
 * Placing it after the `dayLabel == null` return makes that hold by construction rather than by a
 * caller remembering to pass a flag. There is no route through this component that draws an add
 * action without a day named above it.
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
 * @param onAddNote the user wants to write a note on the day named above. **No default, for the
 *   same reason as [onOpenNote]** — a visible control wired to `{}` is indistinguishable from a
 *   working one until somebody taps it, and this one is the only way to write a note on a past day.
 * @param modifier applied to the whole section.
 * @author Phong-Kaster
 */
@Composable
fun CalendarDayNotes(
    dayLabel: String?,
    notes: List<Note>,
    onOpenNote: (Note) -> Unit,
    onAddNote: () -> Unit,
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

        // Above the notes rather than below them. A day can hold any number of notes, and this
        // page scrolls as one with the grid — so an action underneath the list would be pushed
        // further off the bottom of the screen by every note already written on that day, which is
        // exactly when somebody wants to add another.
        CalendarDayNotesAddAction(onClick = onAddNote)

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
 * The way to write a note on the day named above this row.
 *
 * Shaped like a [NoteSummaryRow] — same corner radius, same border, same surface — because it sits
 * directly above a stack of them and a different shape here would read as a different *kind* of
 * thing rather than as an action. What sets it apart is the colour: the plus and the words are
 * `primary`, so the row that *makes* a note is the one coloured thing in a list of rows that only
 * open one.
 *
 * The whole row is the tap target, and it is at least 48dp tall. A short label with a small icon
 * measures about 31dp on its own, and a control that narrow — directly above the notes it is not
 * meant to open — is how somebody taps the wrong thing.
 *
 * @param onClick the user wants a new note on this day.
 * @param modifier applied to the row.
 * @author Phong-Kaster
 */
@Composable
private fun CalendarDayNotesAddAction(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            // Clipped before the ripple is attached, so the ripple stops at the rounded corners.
            .clip(shape = RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(
                // The label and the row's own words are the same sentence, so a screen reader is
                // told what the control does rather than only that it is tappable.
                onClickLabel = stringResource(R.string.add_a_note_to_this_day),
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            // After the click, so the padding is inside the tap target.
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            // `Rounded`, matching the plus on `CoreBottomBar`'s centre button. On this screen the
            // two controls now mean the same thing and are on screen together, so two differently
            // drawn plus glyphs would read as two different kinds of action.
            imageVector = Icons.Rounded.Add,
            // No description: the text beside it says the same thing, and a screen reader that
            // read both would announce this row twice.
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        // Two lines rather than the house default of one-line-plus-marquee, for the same reason
        // as the heading above: German writes "Notiz zu diesem Tag hinzufügen" where English
        // writes "Add a note to this day", and a marquee would set a button's label moving on a
        // page somebody is reading. Whether either locale actually needs the second line is
        // settled by the reference images, not by arithmetic here.
        Text(
            text = stringResource(R.string.add_a_note_to_this_day),
            style = customizedTextStyle(
                fontSize = 14,
                fontWeight = 600,
                color = MaterialTheme.colorScheme.primary,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
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
            onAddNote = {},
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
            onAddNote = {},
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
            onAddNote = {},
        )
    }
}
