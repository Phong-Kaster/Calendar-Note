package com.example.skeleton.ui.fragment.note

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
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
import com.example.skeleton.ui.fragment.note.component.NoteDeleteConfirmSheet
import com.example.skeleton.ui.fragment.note.component.NoteEditor
import com.example.skeleton.ui.fragment.note.model.NoteProblem
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import com.example.skeleton.ui.util.NavigationUtil.safeNavigateUp
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

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
            onDelete = { viewModel.askToDelete() },
            onBack = { safeNavigateUp() },
        )

        // Overlays are siblings of the layout call, never children of it — that is what keeps
        // `NoteLayout` previewable with nothing but a `NoteUiState`. There is one overlay on this
        // screen, so a single flag is enough; a second one would want the mutually-exclusive enum
        // the house rules describe, and the `when` for it would live right here.
        NoteDeleteConfirmSheet(
            enable = uiState.confirmingDelete,
            onCancel = { viewModel.dismissDelete() },
            onConfirm = { viewModel.delete() },
        )

        // Leaving the screen is the Fragment's job, never the layout's — which is why the save
        // does not navigate for itself. The counter, not a flag: it changes on every successful
        // save, so this effect re-runs on every one of them.
        LaunchedEffect(uiState.savedTrigger) {
            if (uiState.savedTrigger > 0) {
                safeNavigateUp()
            }
        }

        // A delete leaves the same way a save does, and says so on the way out. Without the
        // message the user lands back on Home and has to work out from the list whether anything
        // happened — and if they were looking at the note they just removed, "the row is gone" is
        // the only feedback they get.
        LaunchedEffect(uiState.deletedTrigger) {
            if (uiState.deletedTrigger > 0) {
                showToast(message = getString(R.string.note_deleted))
                safeNavigateUp()
            }
        }

        LaunchedEffect(uiState.saveFailed) {
            if (uiState.saveFailed) {
                showToast(message = getString(R.string.we_are_sorry))
                viewModel.consumeSaveFailed()
            }
        }

        // A refused save is not a failure, and it gets its own sentence. "Something went wrong,
        // please try again" — what the effect above says — is an instruction that cannot work
        // here: the note is dated a day that has not arrived, this screen has no date control, and
        // every retry is refused identically. So the message names the day instead, which is the
        // one piece of information that lets the user work out what to do.
        //
        // The user **stays on the screen with their text**, unlike `NoteProblem.Gone` and
        // `Unreadable` below. There is still a note here and it is still savable — on a day that
        // exists.
        LaunchedEffect(uiState.saveRefusedDate) {
            val refusedDate = uiState.saveRefusedDate ?: return@LaunchedEffect

            // The app's own language, not the device's — this app has a picker in Settings, and
            // `Locale.getDefault()` answers for the phone. A localised style rather than a
            // pattern, because word order and the position of the year differ per language.
            val locale = resources.configuration.locales[0]

            showToast(
                message = getString(
                    R.string.a_note_can_only_be_written_on_today_or_an_earlier_day,
                    refusedDate.format(
                        DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(locale),
                    ),
                ),
            )
            viewModel.consumeSaveRefused()
        }

        LaunchedEffect(uiState.problem) {
            val problem = uiState.problem ?: return@LaunchedEffect

            showToast(
                message = when (problem) {
                    NoteProblem.Gone -> getString(R.string.this_note_is_no_longer_there)
                    NoteProblem.Unreadable -> getString(R.string.the_note_could_not_be_opened)
                    NoteProblem.DeleteFailed -> getString(R.string.the_note_could_not_be_deleted)
                },
            )
            viewModel.consumeProblem()

            // A failed delete leaves the note — and the user — exactly where they were, so the
            // screen stays. The other two mean there is nothing on this screen to edit: staying
            // would leave the user typing into an editor with no note behind it.
            if (problem == NoteProblem.DeleteFailed) return@LaunchedEffect
            safeNavigateUp()
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
 * The delete action next to it appears only for a note that has been stored. A brand-new note has
 * nothing to delete, and a control that is visible but inert is a worse answer than one that is not
 * there — it invites the user to work out what they did wrong.
 *
 * @param uiState what to draw.
 * @param onTitleChange forwarded from the heading field.
 * @param onContentChange forwarded from the body field.
 * @param onSave the user asked to keep the note.
 * @param onDelete the user asked to delete the note. It opens a confirmation and nothing else —
 *   see `NoteViewModel.askToDelete()`.
 * @param onBack the user asked to leave without keeping it.
 * @author Phong-Kaster
 */
@Composable
private fun NoteLayout(
    uiState: NoteUiState,
    onTitleChange: (String) -> Unit = {},
    onContentChange: (String) -> Unit = {},
    onSave: () -> Unit = {},
    onDelete: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    CoreLayout(
        modifier = Modifier,
        topBar = {
            CoreTopBar4(
                title = stringResource(R.string.note),
                onBack = onBack,
                actionContent = {
                    if (uiState.deletable) {
                        Text(
                            text = stringResource(R.string.delete),
                            style = customizedTextStyle(
                                fontSize = 14,
                                fontWeight = 500,
                                color = MaterialTheme.colorScheme.error,
                            ),
                            // The gap to Save is the *first* modifier, so it sits outside the tap
                            // target — 8dp of dead space between the destructive control and the
                            // one beside it, not 4. Padding added after `clickable` would stretch
                            // this control *toward* Save, which is the last thing a destructive
                            // action should do.
                            //
                            // `defaultMinSize` sits after `clickable`, so the 48dp minimum touch
                            // target is part of what is clickable rather than dead space around
                            // it. 14sp text with 6dp of padding is about 31dp tall — under the
                            // minimum, and being under it *next to Save* is how somebody deletes
                            // a note they meant to keep. Nothing is painted here, so growing the
                            // box costs no pixels: it only moves the ripple and the tap area out
                            // to where a finger actually lands.
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .clip(shape = RoundedCornerShape(12.dp))
                                .clickable(
                                    onClickLabel = stringResource(R.string.delete_this_note),
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = ripple(bounded = true),
                                    onClick = onDelete,
                                )
                                .defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
                                .wrapContentSize(align = Alignment.Center)
                                .padding(horizontal = 10.dp),
                        )
                    }

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

/** The stored-note state, which is the only one that offers a delete action. */
@Preview(name = "Note - stored")
@Composable
private fun NoteLayoutStoredPreview() {
    MyApplicationTheme {
        NoteLayout(
            uiState = NoteUiState(
                date = LocalDate.of(2026, 3, 14),
                title = "Groceries",
                content = "Coffee, oat milk, the good bread from the corner shop.",
                deletable = true,
            ),
        )
    }
}
