package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.fragment.calendar.model.DayCellState
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalDate

/*
 * --- Why the markers are drawn the way they are (simple story) ---
 *
 * A square can be several things at once: today, and picked, and holding notes. The mistake this
 * component is shaped to avoid is drawing two of those in the same colour on top of each other,
 * where the second one disappears. A prior attempt at this screen drew the has-notes dot in
 * `primary` on top of a `primary`-filled today cell — invisible, on exactly the day a user looks
 * at first — and its unit tests were perfectly happy, because a test can assert that a date is in
 * `datesWithNotes` but not that the dot survived.
 *
 * So the three signals are kept physically apart rather than being merged into one `when`:
 *
 *   - **today** fills an inner square with `primary`. Anything drawn on that fill uses
 *     `onPrimary`.
 *   - **picked** draws a `primary` ring around the *outside*, with a gap. A ring drawn on the
 *     same square as the fill would vanish into it — which is what happens if you write the two
 *     as branches of one `when`, the way the earlier version did.
 *   - **has notes** is a dot under the number, in whatever colour contrasts with what is behind
 *     it: `onPrimary` on today's fill, `primary` on the black ground everywhere else.
 *
 * Every combination is recorded as a reference image in `CalendarScreenshotTest.kt`. The
 * combinations, not the individual states — the individual states were never the broken part.
 */

/** How much of the muted foreground a disabled day keeps. See [CalendarDayCell]'s KDoc. */
private const val DISABLED_DAY_ALPHA = 0.6f

/**
 * One square of the month grid.
 *
 * A future day is drawn dimmed and **cannot be tapped** — `clickable(enabled = false)`, not a
 * click handler that quietly returns. The difference matters to the person holding the phone: a
 * control that ripples and then does nothing is indistinguishable from a bug, while one that does
 * not respond at all reads as switched off.
 *
 * The dimming is `onSurfaceVariant` at 60%, not Material's usual 38%. On this app's pure-black
 * ground 38% measures 2.14:1 against the background — a day number at that weight does not look
 * disabled, it looks missing, and a month with holes in it reads as a rendering fault. 60%
 * measures 3.95:1: clearly quieter than a live day (white, 21:1) and still legible.
 *
 * Neither figure clears WCAG AA's 4.5:1 floor for normal text, and 60% is not claimed to. A
 * disabled control is exempt from that floor (SC 1.4.3), so the choice here is not between
 * passing and failing a threshold — it is between *legibly* switched off and *apparently
 * absent*, and 3.95:1 is the side of that line a person can still read.
 *
 * @param date the day in this square, or null when the square is padding.
 * @param state what this square is; decides the colours and whether taps are accepted.
 * @param isSelected true when this is the day whose notes are being shown.
 * @param hasNotes true when at least one note is already written on this day. Nearly always a
 *   live day — the notes store refuses to date a note after today — but *not* never: if the
 *   clock rolls past midnight while this screen is open, yesterday's `today` is still what the
 *   grid is drawn against, so a note written in that window lands on a square this component is
 *   still calling [DayCellState.Future]. The dot is therefore dimmed to match, rather than
 *   painting a full-strength accent on a square that looks switched off.
 * @param onClick the user tapped this square. Never called for a square that is not selectable.
 * @param modifier applied to the square.
 * @author Phong-Kaster
 */
@Composable
fun CalendarDayCell(
    date: LocalDate?,
    state: DayCellState,
    isSelected: Boolean = false,
    hasNotes: Boolean = false,
    onClick: (LocalDate) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Padding squares hold nothing and accept nothing. Both halves of the condition are checked
    // rather than just one: `date` and `state` arrive from the same source today, and a caller
    // that ever let them disagree would otherwise get a tappable square with no number in it.
    if (date == null || state == DayCellState.OutsideMonth) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        )
        return
    }

    // The number, and the dot under it, are painted in whatever contrasts with what is behind
    // them — which is the fill on today's square and the app's ground everywhere else.
    val foregroundColor = when (state) {
        DayCellState.Today -> MaterialTheme.colorScheme.onPrimary
        DayCellState.Future -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = DISABLED_DAY_ALPHA)
        else -> MaterialTheme.colorScheme.onBackground
    }

    // The dot has to be seen from across the room on a day that is not today, so it takes the
    // accent rather than the ordinary foreground — dimmed to the same weight as the number when
    // the square itself is switched off, so the two never disagree about whether this day is
    // live.
    val markerColor = when (state) {
        DayCellState.Today -> MaterialTheme.colorScheme.onPrimary
        DayCellState.Future -> MaterialTheme.colorScheme.primary.copy(alpha = DISABLED_DAY_ALPHA)
        else -> MaterialTheme.colorScheme.primary
    }

    // Drawn outside the inner square, with a gap, so that a day which is today *and* picked shows
    // both the fill and the ring instead of one swallowing the other.
    val selectionModifier =
        if (isSelected) Modifier.border(
            width = 2.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(14.dp),
        )
        else Modifier

    // Every square that is not today gets the same `outline` edge, live or not. `outlineVariant`
    // was tried for the disabled ones and is wrong here: it is the decorative weight, 2.18:1 on
    // this black ground, and a whole month of it — which is what paging forward shows — reads as
    // a page that failed to render rather than as a month that is switched off. `outline` clears
    // 3:1 and is what `Color.kt` reserves for an edge that carries meaning. The dimmed number is
    // what says "not this one"; the square still says "this is a day".
    val surfaceModifier = when (state) {
        DayCellState.Today -> Modifier.background(
            color = MaterialTheme.colorScheme.primary,
            shape = RoundedCornerShape(10.dp),
        )

        else -> Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = RoundedCornerShape(10.dp),
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            // Clipped before the ripple is attached, so the touch feedback stops at the rounded
            // corners instead of painting a square over the neighbours.
            .clip(shape = RoundedCornerShape(14.dp))
            .then(selectionModifier)
            .clickable(
                enabled = state.isSelectable,
                // Without a label a screen reader announces a bare number and no hint that it
                // does anything. Dropped when the square is switched off, so a day still to come
                // does not carry the name of an action it will not perform — `enabled = false`
                // already marks it disabled, and the two together would be a screen reader
                // saying what this square is for and then refusing to do it.
                onClickLabel = stringResource(R.string.select_day).takeIf { state.isSelectable },
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = { onClick(date) },
            )
            // Without this a screen reader says "3, Select day" and never says which day is the
            // one currently being shown. `clickable(enabled = false)` already contributes the
            // "disabled" half for a day still to come.
            .semantics { selected = isSelected }
            // After the click, so the gap between the ring and the fill is still part of the tap
            // target — the whole square is what the user is aiming at.
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(surfaceModifier),
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
                        fontWeight = if (state == DayCellState.Today || isSelected) 700 else 400,
                        color = foregroundColor,
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                // The strip is always there, with or without a dot in it, so day numbers do not
                // shuffle up and down by a few pixels as notes are written and deleted.
                Box(
                    modifier = Modifier.height(6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (hasNotes) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .background(color = markerColor, shape = CircleShape),
                        )
                    }
                }
            }
        }
    }
}

@Preview(name = "Day cell - past", widthDp = 64, heightDp = 64)
@Composable
private fun CalendarDayCellPastPreview() {
    MyApplicationTheme {
        CalendarDayCell(date = LocalDate.of(2026, 9, 3), state = DayCellState.Past)
    }
}

@Preview(name = "Day cell - today with notes", widthDp = 64, heightDp = 64)
@Composable
private fun CalendarDayCellTodayWithNotesPreview() {
    MyApplicationTheme {
        CalendarDayCell(
            date = LocalDate.of(2026, 9, 10),
            state = DayCellState.Today,
            hasNotes = true,
        )
    }
}

@Preview(name = "Day cell - today and selected", widthDp = 64, heightDp = 64)
@Composable
private fun CalendarDayCellTodayAndSelectedPreview() {
    MyApplicationTheme {
        CalendarDayCell(
            date = LocalDate.of(2026, 9, 10),
            state = DayCellState.Today,
            isSelected = true,
            hasNotes = true,
        )
    }
}

@Preview(name = "Day cell - future", widthDp = 64, heightDp = 64)
@Composable
private fun CalendarDayCellFuturePreview() {
    MyApplicationTheme {
        CalendarDayCell(date = LocalDate.of(2026, 9, 30), state = DayCellState.Future)
    }
}
