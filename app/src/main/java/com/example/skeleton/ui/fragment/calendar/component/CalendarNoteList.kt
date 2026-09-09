package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
 * Lists [notes] — the selected date's notes, oldest first — as cards, or an explanatory line when
 * the selection has none.
 *
 * The empty case is handled here rather than deferred to the caller. It used to be deferred, and
 * the caller never picked it up, so selecting a bare date produced a silent blank gap that looked
 * like a loading failure.
 *
 * @param notes Notes to render, already scoped to the selected date.
 * @param hasSelection True when a date is selected; only then is "no notes" worth saying.
 * @param onEdit Called with the tapped note when its pencil button is pressed.
 * @param onDelete Called with the note id when its trash button is pressed. The caller is
 * expected to confirm before destroying anything.
 * @author Phong-Kaster
 */
@Composable
fun CalendarNoteList(
    notes: List<Note> = emptyList(),
    hasSelection: Boolean = true,
    onEdit: (Note) -> Unit = {},
    onDelete: (Long) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (notes.isEmpty()) {
            if (hasSelection) {
                Text(
                    text = stringResource(R.string.calendar_no_notes_for_date),
                    style = customizedTextStyle(
                        fontSize = 14,
                        fontWeight = 400,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                    modifier = Modifier.padding(vertical = 8.dp),
                )
            }
            return@Column
        }

        notes.forEach { note ->
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = note.title,
                        style = customizedTextStyle(
                            fontSize = 14,
                            fontWeight = 400,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        maxLines = 3,
                        modifier = Modifier
                            .weight(1f)
                            .padding(vertical = 12.dp),
                    )

                    IconButton(onClick = { onEdit(note) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_note),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    IconButton(onClick = { onDelete(note.id) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_note),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
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

@Preview(name = "Empty with a date selected")
@Composable
private fun CalendarNoteListEmptyPreview() {
    CalendarNoteList(notes = emptyList(), hasSelection = true)
}

@Preview(name = "Empty, nothing selected")
@Composable
private fun CalendarNoteListNoSelectionPreview() {
    CalendarNoteList(notes = emptyList(), hasSelection = false)
}
