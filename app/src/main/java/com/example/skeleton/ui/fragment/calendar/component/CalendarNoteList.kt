package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Lists [notes] — the selected date's notes, oldest first. Renders nothing extra when empty;
 * the caller decides whether an empty state message is needed elsewhere.
 *
 * @param notes Notes to render, already scoped to the selected date.
 * @param onEdit Called with the tapped note when its pencil button is pressed.
 * @param onDelete Called with the note id when its trash button is pressed.
 * @author Phong-Kaster
 */
@Composable
fun CalendarNoteList(
    notes: List<Note> = emptyList(),
    onEdit: (Note) -> Unit = {},
    onDelete: (Long) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        notes.forEach { note ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = note.title,
                    style = customizedTextStyle(
                        fontSize = 14,
                        fontWeight = 400,
                        color = MaterialTheme.colorScheme.onSurface,
                    ),
                    modifier = Modifier.weight(1f),
                )

                IconButton(
                    onClick = {
                        onEdit(note)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit_note),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }

                IconButton(
                    onClick = {
                        onDelete(note.id)
                    },
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_note),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
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
