package com.example.skeleton.ui.fragment.home.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.component.NoteSummaryRow
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalDate

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
 * The rows themselves are [NoteSummaryRow], shared with the Calendar screen's day list. Here they
 * print their date, because Home mixes every day together and the date is the only thing telling
 * two rows apart.
 *
 * The list arrives already ordered — that is the store's promise, not this screen's — so nothing
 * here sorts it.
 *
 * [onOpenNote] has **no default**, for the same reason `CoreBottomBar.onCreateNote` has none: a row
 * draws a truncated note, so a tap that does nothing leaves the rest of the text unreachable — and
 * it looks exactly like a working screen. Without a default, a new host has to decide what a tap
 * means there.
 *
 * @param notes the notes to show, most recently touched first.
 * @param onOpenNote the user tapped a row and wants that note opened.
 * @param modifier applied to the scrolling container, or to the empty state when there is nothing
 *   to scroll.
 * @author Phong-Kaster
 */
@Composable
fun HomeNoteList(
    notes: List<Note>,
    onOpenNote: (Note) -> Unit,
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
            NoteSummaryRow(
                note = note,
                onClick = { onOpenNote(note) },
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
            onOpenNote = {},
        )
    }
}

@Preview(name = "Notes - empty", widthDp = 360, heightDp = 420)
@Composable
private fun HomeNoteListEmptyPreview() {
    MyApplicationTheme {
        HomeNoteList(
            notes = emptyList(),
            onOpenNote = {},
        )
    }
}
