package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Lists [notes] — the selected date's notes, oldest first. Renders nothing extra when empty;
 * the caller decides whether an empty state message is needed elsewhere.
 *
 * @param notes Notes to render, already scoped to the selected date.
 * @author Phong-Kaster
 */
@Composable
fun CalendarNoteList(notes: List<Note> = emptyList()) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        notes.forEach { note ->
            Text(
                text = note.title,
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 400,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
            )
        }
    }
}

@Preview
@Composable
private fun CalendarNoteListPreview() {
    CalendarNoteList(
        notes = listOf(
            Note(id = 1, epochDay = 0, title = "Dentist appointment", createdAt = 0),
            Note(id = 2, epochDay = 0, title = "Buy birthday gift", createdAt = 0),
        ),
    )
}

@Preview(name = "Empty")
@Composable
private fun CalendarNoteListEmptyPreview() {
    CalendarNoteList(notes = emptyList())
}
