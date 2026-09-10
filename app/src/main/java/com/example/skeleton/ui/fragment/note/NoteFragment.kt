package com.example.skeleton.ui.fragment.note

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.component.CoreTopBar4
import com.example.skeleton.ui.fragment.note.component.NoteEditor
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import com.example.skeleton.ui.util.NavigationUtil.safeNavigateUp
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate

/**
 * The Note screen: write a new note, or edit one that already exists.
 *
 * Which of the two it is arrives in the Fragment's arguments, and [argumentsFor] is the only place
 * allowed to build them. This project has no Safe Args plugin, so a navigation argument is really
 * just a string key agreed between two files — and a key typed correctly in one place and
 * incorrectly in the other produces no compiler error, no crash, and a screen that quietly opens a
 * blank note instead of the one that was tapped. One factory function means there is one spelling.
 *
 * @author Phong-Kaster
 */
class NoteFragment : CoreFragment() {

    private val viewModel: NoteViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val noteId = arguments?.getLong(ARGUMENT_NOTE_ID, NEW_NOTE_ID) ?: NEW_NOTE_ID
        val epochDay = arguments?.getLong(ARGUMENT_EPOCH_DAY, UNSET_EPOCH_DAY) ?: UNSET_EPOCH_DAY

        viewModel.openNote(
            // The navigation contract says -1 for "a note that does not exist yet"; the store says
            // 0, because 0 is what Room reads as "hand this row a fresh id". The translation
            // happens here, at the edge of the screen, so that neither side has to know the
            // other's spelling of the same idea.
            noteId = if (noteId == NEW_NOTE_ID) Note.UNSAVED_ID else noteId,
            date = if (epochDay == UNSET_EPOCH_DAY) LocalDate.now() else LocalDate.ofEpochDay(epochDay),
        )
    }

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        NoteLayout(
            uiState = uiState,
            onTitleChange = { value -> viewModel.setTitle(value = value) },
            onContentChange = { value -> viewModel.setContent(value = value) },
            onSave = { viewModel.save() },
            onBack = { safeNavigateUp() },
        )

        // Leaving the screen is the Fragment's job, never the layout's — which is why the save
        // does not navigate for itself. The counter, not a flag: it changes on every successful
        // save, so this effect re-runs on every one of them.
        LaunchedEffect(uiState.savedTrigger) {
            if (uiState.savedTrigger > 0) {
                safeNavigateUp()
            }
        }

        LaunchedEffect(uiState.saveFailed) {
            if (uiState.saveFailed) {
                showToast(message = getString(R.string.we_are_sorry))
                viewModel.consumeSaveFailed()
            }
        }
    }

    companion object {

        /** Key of the row id to edit. */
        private const val ARGUMENT_NOTE_ID = "noteId"

        /** Key of the day a new note belongs to, as `LocalDate.toEpochDay()`. */
        private const val ARGUMENT_EPOCH_DAY = "noteEpochDay"

        /**
         * The id that means "there is no stored note yet".
         *
         * `-1` and not `0`, so it cannot be confused with a real row id *or* with the `0` the
         * store uses for the same idea. Both directions of that confusion are silent: `0` would
         * read as a note to load, and a missing argument defaulting to `0` would read as a note to
         * overwrite.
         */
        const val NEW_NOTE_ID = -1L

        /**
         * The epoch day that means "nobody said which day; use today".
         *
         * `Long.MIN_VALUE` rather than something tidy like `-1`, because -1 *is* a real day
         * (1969-12-31) and a sentinel that is also a legal value is a bug waiting for the one user
         * who hits it.
         */
        private const val UNSET_EPOCH_DAY = Long.MIN_VALUE

        /**
         * Builds the arguments this screen expects.
         *
         * @param date the day the note belongs to.
         * @param noteId the row id to edit; leave it out to start a new note.
         * @author Phong-Kaster
         */
        fun argumentsFor(date: LocalDate, noteId: Long = NEW_NOTE_ID): Bundle = bundleOf(
            ARGUMENT_NOTE_ID to noteId,
            ARGUMENT_EPOCH_DAY to date.toEpochDay(),
        )
    }
}

/**
 * The Note screen's UI: a scrolling editor with the save action pinned above it.
 *
 * Save sits in the top bar rather than at the end of the content on purpose. It is the one control
 * a user must always be able to reach, and a control that lives at the bottom of a scrolling
 * editor moves further away with every word they type.
 *
 * Pure UI — it renders the state it is handed and navigates nowhere, which is what lets the
 * previews at the bottom of this file draw it with made-up data and no database behind them.
 *
 * @param uiState what to draw.
 * @param onTitleChange forwarded from the heading field.
 * @param onContentChange forwarded from the body field.
 * @param onSave the user asked to keep the note.
 * @param onBack the user asked to leave without keeping it.
 * @author Phong-Kaster
 */
@Composable
private fun NoteLayout(
    uiState: NoteUiState,
    onTitleChange: (String) -> Unit = {},
    onContentChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    CoreLayout(
        modifier = Modifier,
        topBar = {
            CoreTopBar4(
                title = stringResource(R.string.note),
                onBack = onBack,
                actionContent = {
                    Text(
                        text = stringResource(R.string.save),
                        style = customizedTextStyle(
                            fontSize = 14,
                            fontWeight = 600,
                            color = MaterialTheme.colorScheme.onPrimary,
                        ),
                        modifier = Modifier
                            .clip(shape = RoundedCornerShape(12.dp))
                            .background(color = MaterialTheme.colorScheme.primary)
                            .clickable(onClick = onSave)
                            .padding(vertical = 6.dp, horizontal = 14.dp),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        content = {
            NoteEditor(
                date = uiState.date,
                title = uiState.title,
                content = uiState.content,
                onTitleChange = onTitleChange,
                onContentChange = onContentChange,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}

@Preview(name = "Note - new")
@Composable
private fun NoteLayoutNewPreview() {
    MyApplicationTheme {
        NoteLayout(uiState = NoteUiState(date = LocalDate.of(2026, 3, 14)))
    }
}

@Preview(name = "Note - written")
@Composable
private fun NoteLayoutWrittenPreview() {
    MyApplicationTheme {
        NoteLayout(
            uiState = NoteUiState(
                date = LocalDate.of(2026, 3, 14),
                title = "Groceries",
                content = "Coffee, oat milk, the good bread from the corner shop.",
            ),
        )
    }
}
