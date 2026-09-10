package com.example.skeleton.ui.fragment.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
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
 * The body of the Home screen: every note the user has written, newest first.
 *
 * Two things this composable is responsible for, both of which are easy to leave out and painful
 * to notice afterwards:
 *
 * - **It scrolls.** A `LazyColumn`, not a `Column`, because the list has no upper size. A fixed
 *   layout pushes the oldest notes off the bottom of the screen with no way to reach them.
 * - **It says when it is empty.** A blank region reads as "something failed to load", not as "you
 *   have not written anything yet". The message is the whole difference.
 *
 * The list arrives already ordered — that is the store's promise, not this screen's — so nothing
 * here sorts it.
 *
 * @param notes the notes to show, most recently touched first.
 * @param modifier applied to the scrolling container, or to the empty state when there is nothing
 *   to scroll.
 * @author Phong-Kaster
 */
@Composable
fun HomeNoteList(
    notes: List<Note>,
    modifier: Modifier = Modifier,
) {
    if (notes.isEmpty()) {
        HomeNoteListEmpty(modifier = modifier.fillMaxSize())
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = notes,
            key = { note -> note.id },
        ) { note ->
            NoteRow(
                note = note,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

/**
 * What the user sees before writing anything.
 *
 * Centred in the whole content area rather than tucked under the top bar, so it reads as "this
 * screen is empty" instead of as one stray line that failed to load.
 *
 * @param modifier applied to the centring box.
 * @author Phong-Kaster
 */
@Composable
private fun HomeNoteListEmpty(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.padding(horizontal = 32.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.no_notes_yet),
            style = customizedTextStyle(
                fontSize = 16,
                fontWeight = 500,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            textAlign = TextAlign.Center,
        )
    }
}

/**
 * One note in the list: its heading, the day it belongs to, and the start of its body.
 *
 * Both texts stop after a fixed number of lines with an ellipsis instead of growing to fit —
 * otherwise one long note fills the screen and hides every note underneath it. Nothing is lost by
 * cutting: opening the note shows all of it.
 *
 * The heading comes from [Note.displayTitle] — the title when there is one, the first written line
 * of the body when there is not. A note with neither still needs to say *something*, so the row
 * draws a placeholder; an unlabelled row looks like a rendering bug.
 *
 * @param note the note to draw.
 * @param modifier applied to the card.
 * @author Phong-Kaster
 */
@Composable
private fun NoteRow(
    note: Note,
    modifier: Modifier = Modifier,
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
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp),
            )
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

@Preview(name = "Notes", widthDp = 360, heightDp = 420)
@Composable
private fun HomeNoteListPreview() {
    MyApplicationTheme {
        HomeNoteList(
            notes = listOf(
                Note(
                    id = 1L,
                    date = LocalDate.of(2026, 3, 14),
                    title = "Groceries",
                    content = "Coffee, oat milk, the good bread from the corner shop.",
                    createdAt = 1_773_000_000_000L,
                    updatedAt = 1_773_000_000_000L,
                ),
                Note(
                    id = 2L,
                    date = LocalDate.of(2026, 3, 13),
                    title = "",
                    content = "No title on this one, so the first line of the body becomes the heading.",
                    createdAt = 1_772_900_000_000L,
                    updatedAt = 1_772_900_000_000L,
                ),
            ),
        )
    }
}

@Preview(name = "Notes - empty", widthDp = 360, heightDp = 420)
@Composable
private fun HomeNoteListEmptyPreview() {
    MyApplicationTheme {
        HomeNoteList(notes = emptyList())
    }
}
