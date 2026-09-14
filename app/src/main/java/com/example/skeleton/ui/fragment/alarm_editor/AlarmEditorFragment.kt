package com.example.skeleton.ui.fragment.alarm_editor

import android.os.Bundle
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.ui.component.CoreTopBar4
import com.example.skeleton.ui.fragment.alarm_editor.component.AlarmEditor
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import com.example.skeleton.ui.util.NavigationUtil.safeNavigateUp
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The alarm editor: write a new alarm, or edit one that already exists.
 *
 * Which of the two it is arrives in the Fragment's arguments, and [argumentsFor] is the only place
 * allowed to build them. This project has no Safe Args plugin, so a navigation argument is really
 * just a string key agreed between two files — and a key typed correctly in one place and
 * incorrectly in the other produces no compiler error, no crash, and a screen that quietly opens a
 * blank alarm instead of the one that was tapped. One factory function means there is one spelling.
 *
 * Both callers are on the Alarms screen: its floating action button arrives here with no id, and a
 * tap on a row arrives with that row's id.
 *
 * @author Phong-Kaster
 */
class AlarmEditorFragment : CoreFragment() {

    private val viewModel: AlarmEditorViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val alarmId = arguments?.getLong(ARGUMENT_ALARM_ID, NEW_ALARM_ID) ?: NEW_ALARM_ID

        viewModel.openAlarm(
            // The navigation contract says -1 for "an alarm that does not exist yet"; the store says
            // 0, because 0 is what Room reads as "hand this row a fresh id". The translation happens
            // here, at the edge of the screen, so that neither side has to know the other's spelling
            // of the same idea.
            alarmId = if (alarmId == NEW_ALARM_ID) Alarm.UNSAVED_ID else alarmId,
        )
    }

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        AlarmEditorLayout(
            uiState = uiState,
            onMessageChange = { value -> viewModel.setMessage(value = value) },
            onTimeChange = { hourOfDay, minute ->
                viewModel.setTime(hourOfDay = hourOfDay, minute = minute)
            },
            onSave = { viewModel.save() },
            onBack = { safeNavigateUp() },
        )

        // Leaving the screen is the Fragment's job, never the layout's — which is why the save does
        // not navigate for itself. The counter, not a flag: it changes on every successful save, so
        // this effect re-runs on every one of them.
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

        // A refused save is not a failure, and it gets its own sentence. "Something went wrong,
        // please try again" — what the effect above says — is an instruction that cannot work here:
        // the alarm has nothing written on it, and every retry is refused identically. So the
        // message names what is missing instead.
        //
        // The user **stays on the screen with their time**, unlike the open failure below. There is
        // still an alarm here and it is still savable — once it says something.
        LaunchedEffect(uiState.saveRefusedBlank) {
            if (uiState.saveRefusedBlank) {
                showToast(message = getString(R.string.an_alarm_needs_something_written_on_it))
                viewModel.consumeSaveRefused()
            }
        }

        // There is no alarm behind this screen: staying would leave the user typing into an editor
        // with nothing under it, whose save would file a *second* alarm beside the one they meant
        // to change.
        LaunchedEffect(uiState.openFailed) {
            if (uiState.openFailed) {
                showToast(message = getString(R.string.the_alarm_could_not_be_opened))
                viewModel.consumeOpenFailed()
                safeNavigateUp()
            }
        }
    }

    companion object {

        /** Key of the row id to edit. */
        private const val ARGUMENT_ALARM_ID = "alarmId"

        /**
         * The id that means "there is no stored alarm yet".
         *
         * `-1` and not `0`, so it cannot be confused with a real row id *or* with the `0` the store
         * uses for the same idea. Both directions of that confusion are silent: `0` would read as
         * an alarm to load, and a missing argument defaulting to `0` would read as an alarm to
         * overwrite. Same sentinel, same reasoning, as `NoteFragment.NEW_NOTE_ID`.
         */
        const val NEW_ALARM_ID = -1L

        /**
         * Builds the arguments this screen expects.
         *
         * @param alarmId the row id to edit; leave it out to start a new alarm.
         * @author Phong-Kaster
         */
        fun argumentsFor(alarmId: Long = NEW_ALARM_ID): Bundle = bundleOf(
            ARGUMENT_ALARM_ID to alarmId,
        )
    }
}

/**
 * The alarm editor's UI: a scrolling editor with the save action pinned above it.
 *
 * Save sits in the top bar rather than at the end of the content on purpose. It is the one control
 * a user must always be able to reach, and a control that lives at the bottom of a scrolling editor
 * moves further away with every word they type — and further still once the keyboard is up.
 *
 * Pure UI — it renders the state it is handed and navigates nowhere, which is what lets the previews
 * at the bottom of this file draw it with made-up data and no database behind them.
 *
 * There is no delete action here. Nothing in the app can remove an alarm yet, and a control that is
 * visible but inert is a worse answer than one that is not there.
 *
 * @param uiState what to draw.
 * @param onMessageChange forwarded from the message field.
 * @param onTimeChange forwarded from the time control.
 * @param onSave the user asked to keep the alarm.
 * @param onBack the user asked to leave without keeping it.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmEditorLayout(
    uiState: AlarmEditorUiState,
    onMessageChange: (String) -> Unit = {},
    onTimeChange: (hourOfDay: Int, minute: Int) -> Unit = { _, _ -> },
    onSave: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    CoreLayout(
        modifier = Modifier,
        // The spinner is not politeness. The time control below takes its hour and minute at its
        // first composition and never looks at them again, so drawing the editor before the alarm
        // has been read would show — and then save — the wrong time. See `AlarmEditorUiState`.
        showLoading = uiState.isLoading,
        topBar = {
            CoreTopBar4(
                title = stringResource(R.string.alarm),
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
                            // The full call, not the one-argument shorthand. This is the screen's
                            // primary action: it needs a label a screen reader can announce, and a
                            // ripple bounded by the rounded shape above rather than the platform
                            // default spilling past its corners.
                            .clickable(
                                onClickLabel = stringResource(R.string.save),
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = true),
                                onClick = onSave,
                            )
                            .padding(vertical = 6.dp, horizontal = 14.dp),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            )
        },
        content = {
            AlarmEditor(
                message = uiState.message,
                hourOfDay = uiState.hourOfDay,
                minute = uiState.minute,
                onMessageChange = onMessageChange,
                onTimeChange = onTimeChange,
                modifier = Modifier.fillMaxSize(),
            )
        },
    )
}

@Preview(name = "Alarm editor - new")
@Composable
private fun AlarmEditorLayoutNewPreview() {
    MyApplicationTheme {
        AlarmEditorLayout(uiState = AlarmEditorUiState())
    }
}

@Preview(name = "Alarm editor - written")
@Composable
private fun AlarmEditorLayoutWrittenPreview() {
    MyApplicationTheme {
        AlarmEditorLayout(
            uiState = AlarmEditorUiState(
                message = "Take the bread out of the freezer",
                hourOfDay = 21,
                minute = 30,
            ),
        )
    }
}
