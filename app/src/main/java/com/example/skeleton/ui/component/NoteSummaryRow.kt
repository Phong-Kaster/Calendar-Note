package com.example.skeleton.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

/**
 * One note as a list row: its heading, optionally the day it belongs to, and the start of its
 * body.
 *
 * Shared by the two screens that list notes — Home, which lists every note there is, and the
 * Calendar screen's section for one picked day. It lives in `ui/component/` rather than inside
 * either of them for that reason: two near-identical rows is how the two lists end up quietly
 * looking different, and a user who taps the same card on two screens should get the same card.
 *
 * Both texts stop after a fixed number of lines with an ellipsis instead of growing to fit —
 * otherwise one long note fills the screen and hides every note underneath it. Nothing is lost by
 * cutting: opening the note shows all of it.
 *
 * The heading comes from [Note.displayTitle] — the title when there is one, the first written line
 * of the body when there is not. A note with neither still needs to say *something*, so the row
 * draws a placeholder; an unlabelled row looks like a rendering bug.
 *
 * The whole card is the tap target, not the heading inside it. A row is what the user sees as one
 * thing, and a tap that only counts when it lands on the text is a row that seems to ignore half
 * the taps aimed at it.
 *
 * @param note the note to draw.
 * @param onClick the user tapped this row.
 * @param modifier applied to the card.
 * @param showDate whether to print the note's day under the heading. True on Home, where rows from
 *   every day sit in one list and the date is the only thing telling them apart. **False on the
 *   Calendar screen**, where every row in the list is on the day named in the heading above it —
 *   repeating that date on every card says nothing and pushes the body text down.
 * @author Phong-Kaster
 */
@Composable
fun NoteSummaryRow(
    note: Note,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDate: Boolean = true,
) {
    val heading =
        if (note.displayTitle.isNotBlank()) note.displayTitle
        else stringResource(R.string.untitled_note)

    // The date is formatted in the language the *app* is showing, which is not always the
    // language of the device: this app has its own picker in Settings. `Locale.getDefault()` —
    // what the formatter uses when nobody tells it otherwise — answers for the device, so a user
    // who switched the app to German could end up reading "Mar 14, 2026" under German copy.
    // Compose's configuration is the one that follows the picker.
    val locale = LocalConfiguration.current.locales[0]

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Clipped first so the ripple stops at the rounded corners.
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
                // Without the label a screen reader reads the row's text and gives no hint that
                // it is a door.
                onClickLabel = stringResource(R.string.open_note),
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            // After the click, so the padding is inside the tap target.
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = heading,
            style = customizedTextStyle(fontSize = 16, fontWeight = 600),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )

        // One deliberate deviation from the house rule that single-line text gets
        // `Modifier.basicMarquee(...)`: a marquee animates, and a list where every row's date
        // scrolls sideways forever is worse than one that cannot happen — a medium-format date
        // does not overflow a row this wide. Ellipsis is the fallback if it ever does.
        if (showDate) {
            Text(
                text = note.date.format(
                    DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(locale),
                ),
                style = customizedTextStyle(
                    fontSize = 12,
                    fontWeight = 500,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (note.content.isNotBlank()) {
            Text(
                text = note.content,
                style = customizedTextStyle(
                    fontSize = 14,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Note row - with date", widthDp = 360)
@Composable
private fun NoteSummaryRowPreview() {
    MyApplicationTheme {
        NoteSummaryRow(
            note = Note(
                id = 1L,
                date = LocalDate.of(2026, 3, 14),
                title = "Groceries",
                content = "Coffee, oat milk, the good bread from the corner shop.",
                createdAt = 1_773_000_000_000L,
                updatedAt = 1_773_000_000_000L,
            ),
            onClick = {},
        )
    }
}

@Preview(name = "Note row - without date", widthDp = 360)
@Composable
private fun NoteSummaryRowWithoutDatePreview() {
    MyApplicationTheme {
        NoteSummaryRow(
            note = Note(
                id = 2L,
                date = LocalDate.of(2026, 3, 14),
                title = "",
                content = "No title on this one, so the first line of the body becomes the heading.",
                createdAt = 1_772_900_000_000L,
                updatedAt = 1_772_900_000_000L,
            ),
            onClick = {},
            showDate = false,
        )
    }
}
