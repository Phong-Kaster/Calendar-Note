package com.example.skeleton.ui.fragment.alarms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.alarm_editor.AlarmEditorFragment
import com.example.skeleton.ui.fragment.alarms.component.AlarmDeleteConfirmSheet
import com.example.skeleton.ui.fragment.alarms.component.AlarmRow
import com.example.skeleton.ui.fragment.alarms.component.AlarmsEmptyState
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.util.NavigationUtil.safeNavigate
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * How much empty space the alarm list keeps below its last row.
 *
 * **It is the floating action button's own size plus its margin on both sides**, and every part of
 * that sum is load-bearing. The button floats *over* the list rather than beside it, so without this
 * gap the final alarm sits underneath it: half-covered, and — worse — untappable, because the button
 * takes the touch. The bug only appears once there are enough alarms to fill the screen, which is
 * exactly the state nobody has while building the feature and every real user reaches eventually.
 *
 * 56dp is Material's own `FloatingActionButton` size and 16dp is the margin `Scaffold` leaves around
 * it; the margin is counted **twice** so the last row clears the button entirely rather than ending
 * flush against it. The arithmetic is written out rather than collapsed to `88.dp` so that a reader
 * can see where it came from and check it.
 */
private val ALARM_LIST_BOTTOM_PADDING = 56.dp + 16.dp + 16.dp

/**
 * The Alarms screen: the fourth top-level tab.
 *
 * It lists every alarm the store holds and offers three things to do with one: open it, switch it on
 * or off without opening it, and remove it. Writing a new alarm and opening an existing one lead to
 * the same place, the alarm editor; the only difference is which alarm it opens with, and that
 * travels as a navigation argument.
 *
 * Removing an alarm never happens on one tap — the bin on a row only *asks*. The question and the
 * answer both live in `AlarmsViewModel`, so the store stays unreachable from a single tap by
 * construction rather than by the layout being careful.
 *
 * Thin, like every Fragment here: it owns the ViewModel, the navigation and the messages, and hands
 * the drawing to [AlarmsLayout].
 *
 * @author Phong-Kaster
 */
class AlarmsFragment : CoreFragment() {
    private val viewModel: AlarmsViewModel by viewModel()

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        AlarmsLayout(
            uiState = uiState,
            onCreateAlarm = { openAlarmEditor() },
            onOpenAlarm = { alarm -> openAlarmEditor(alarmId = alarm.id) },
            onToggleAlarm = { alarm, enabled ->
                viewModel.setEnabled(alarm = alarm, enabled = enabled)
            },
            onAskToDelete = { alarm -> viewModel.askToDelete(alarmId = alarm.id) },
        )

        // Overlays are siblings of the layout call, never children of it — that is what keeps
        // `AlarmsLayout` previewable with nothing but an `AlarmsUiState`. There is one overlay on
        // this screen, so a single nullable id is enough; a second one would want the
        // mutually-exclusive enum the house rules describe, and the `when` for it would live here.
        AlarmDeleteConfirmSheet(
            enable = uiState.confirmingDelete,
            onCancel = { viewModel.dismissDelete() },
            onConfirm = { viewModel.confirmDelete() },
        )

        // The row leaves the list on its own — the store's list is live — but "a row vanished" is
        // thin feedback for the one action in this app that cannot be undone, and thinner still if
        // the user's eye was elsewhere. The counter, not a flag: it changes on every successful
        // delete, so this effect re-runs on every one of them.
        LaunchedEffect(uiState.deletedTrigger) {
            if (uiState.deletedTrigger > 0) {
                showToast(message = getString(R.string.alarm_deleted))
                viewModel.consumeDeleted()
            }
        }

        LaunchedEffect(uiState.deleteFailed) {
            if (uiState.deleteFailed) {
                showToast(message = getString(R.string.the_alarm_could_not_be_deleted))
                viewModel.consumeDeleteFailed()
            }
        }

        // A switch that snapped back is a control that looks like it ignored the tap. It gets its
        // own sentence rather than the delete one: nothing was removed here, and telling somebody
        // their alarm could not be deleted when they only tried to switch it off would be alarming
        // and wrong.
        LaunchedEffect(uiState.toggleFailed) {
            if (uiState.toggleFailed) {
                showToast(message = getString(R.string.the_alarm_could_not_be_changed))
                viewModel.consumeToggleFailed()
            }
        }
    }

    /**
     * Opens the alarm editor, for a stored alarm or for a brand-new one.
     *
     * **The guard is "where am I?", not a timer.** Two taps landing inside the same moment — a double
     * tap on the floating action button, or on a row — must not push two copies of the editor onto
     * the back stack, because leaving the editor would then take two presses of back and the second
     * press would land the user on a screen they thought they had already left. `safeNavigate` does
     * not debounce for itself; it only swallows exceptions.
     *
     * `NavigationUtil.canNavigate()` is deliberately **not** used for this. It is one process-wide
     * 800 ms window shared by every control in the app, and `CoreBottomBar` arms it even for a tab
     * tap that navigates nowhere — so tapping "Alarms" while already on Alarms would leave this
     * button and every row dead for the next 800 ms, showing a ripple and doing nothing, for a
     * reason the user cannot see. Asking the graph has no dead window at all: `navigate` moves
     * `currentDestination` synchronously, so the second tap of a double tap is already standing on
     * the editor and is refused, while a deliberate tap a moment later still works.
     *
     * @param alarmId the row id to edit; left out to start a new alarm.
     * @author Phong-Kaster
     */
    private fun openAlarmEditor(alarmId: Long = AlarmEditorFragment.NEW_ALARM_ID) {
        val currentDestination = runCatching {
            findNavController().currentDestination?.id
        }.getOrNull()

        if (currentDestination != R.id.alarmsFragment) return

        safeNavigate(
            destination = R.id.toAlarmEditor,
            // Built by the destination's own factory rather than by a `bundleOf` here. There is no
            // Safe Args plugin in this project, so an argument is really just a string key agreed
            // between two files — and a key spelled correctly in one and wrongly in the other
            // produces no compiler error and no crash, only an editor that quietly opens a blank
            // alarm instead of the one that was tapped.
            bundle = AlarmEditorFragment.argumentsFor(alarmId = alarmId),
        )
    }
}

/**
 * The Alarms screen, drawn: a title, the bottom bar, the list of alarms, and the round button that
 * writes a new one.
 *
 * Pure UI. It takes state and callbacks and nothing else, which is what lets the previews below
 * render the whole screen with no ViewModel, no Koin and no database behind them.
 *
 * **`onCreateNote = {}` on the bottom bar is a deliberate no-op and not an oversight.**
 * `CoreBottomBar` refuses a default for that lambda precisely so nobody ships the app's create
 * action as a dead button — but this destination sets `hidesCreateButton`, so the shared centre "+"
 * is not drawn here at all and there is no button for the empty lambda to disappoint. Alarms has its
 * own create action instead, and it is the floating button below: an alarm is not a note, and one
 * button that made either depending on which tab you were standing on would be a button whose
 * meaning you have to remember.
 *
 * @param uiState what to draw.
 * @param onCreateAlarm the user wants to write a new alarm.
 * @param onOpenAlarm the user tapped a row and wants that alarm opened.
 * @param onToggleAlarm the user moved a row's switch. True means "arm this alarm".
 * @param onAskToDelete the user tapped a row's bin. It opens a confirmation and nothing else — see
 *   `AlarmsViewModel.askToDelete()`.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmsLayout(
    uiState: AlarmsUiState,
    onCreateAlarm: () -> Unit = {},
    onOpenAlarm: (Alarm) -> Unit = {},
    onToggleAlarm: (Alarm, Boolean) -> Unit = { _, _ -> },
    onAskToDelete: (Alarm) -> Unit = {},
) {
    CoreLayout(
        modifier = Modifier,
        topBar = { CoreTopBar(title = stringResource(R.string.alarms)) },
        bottomBar = { CoreBottomBar(onCreateNote = {}) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateAlarm,
                // Both colours come from the theme, and they are taken as a *pair*. Material pairs
                // `primaryContainer` with `onPrimaryContainer` on purpose — the two were measured
                // against each other (9.12:1) — so picking one from the scheme and the other by
                // hand is how a button ends up legible in the designer's head and invisible on the
                // device.
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                content = {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        // Not null. This button carries no label, so without a description the
                        // screen's only action is an unnamed button to anybody using a screen
                        // reader — and "button" is not an instruction.
                        contentDescription = stringResource(R.string.add_alarm),
                    )
                },
            )
        },
        content = {
            if (uiState.isEmpty) {
                AlarmsEmptyState()
            } else {
                AlarmsList(
                    alarms = uiState.alarms,
                    onOpenAlarm = onOpenAlarm,
                    onToggleAlarm = onToggleAlarm,
                    onAskToDelete = onAskToDelete,
                )
            }
        },
    )
}

/**
 * Every alarm the user has, one row each.
 *
 * A `LazyColumn` and not a `Column`, because the list has no upper size: a fixed layout pushes the
 * later alarms off the bottom of the screen with no way to reach them.
 *
 * The list arrives already ordered — earliest time of day first — and that is the store's promise,
 * not this screen's, so nothing here sorts it.
 *
 * @param alarms the alarms to show, earliest first.
 * @param onOpenAlarm the user tapped a row.
 * @param onToggleAlarm the user moved a row's switch.
 * @param onAskToDelete the user tapped a row's bin.
 * @param modifier applied to the scrolling container.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmsList(
    alarms: List<Alarm>,
    onOpenAlarm: (Alarm) -> Unit = {},
    onToggleAlarm: (Alarm, Boolean) -> Unit = { _, _ -> },
    onAskToDelete: (Alarm) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = ALARM_LIST_BOTTOM_PADDING),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(
            items = alarms,
            // Readable and unique at once. The row id alone would do the job, but a debugger
            // stopping on "7" says nothing about what 7 is.
            key = { alarm -> "AlarmRow-${alarm.id}" },
        ) { alarm ->
            AlarmRow(
                alarm = alarm,
                onClick = { onOpenAlarm(alarm) },
                onToggleEnabled = { enabled -> onToggleAlarm(alarm, enabled) },
                onDelete = { onAskToDelete(alarm) },
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Preview(name = "Alarms - empty", widthDp = 360, heightDp = 780)
@Composable
private fun AlarmsLayoutEmptyPreview() {
    // Sized as the real full screen rather than as the content area. A preview smaller than the
    // thing it renders does not record as clipped — it records as empty, so a too-small frame here
    // would photograph a working empty state and a broken one identically.
    MyApplicationTheme(
        content = {
            AlarmsLayout(uiState = AlarmsUiState())
        },
    )
}

@Preview(name = "Alarms - with alarms", widthDp = 360, heightDp = 780)
@Composable
private fun AlarmsLayoutPopulatedPreview() {
    // The state the empty preview cannot show anything about, and the one where the layout can
    // actually go wrong: the rows, their order, and the gap the floating button needs under the last
    // of them. Three alarms rather than one, because a single row would sit nowhere near the button.
    //
    // The middle alarm is switched **off** on purpose. On and off next to each other is the only way
    // to see whether the switch reads at a glance — a list where every switch points the same way
    // looks perfectly fine even when the two states are indistinguishable.
    MyApplicationTheme(
        content = {
            AlarmsLayout(
                uiState = AlarmsUiState(
                    alarms = listOf(
                        Alarm(
                            id = 1L,
                            message = "Take the bread out of the freezer",
                            hourOfDay = 7,
                            minute = 30,
                            enabled = true,
                            createdAt = 1_773_000_000_000L,
                        ),
                        Alarm(
                            id = 2L,
                            message = "Leave for the dentist",
                            hourOfDay = 14,
                            minute = 5,
                            enabled = false,
                            createdAt = 1_772_950_000_000L,
                        ),
                        Alarm(
                            id = 3L,
                            message = "Call the dentist about moving next Tuesday's appointment, " +
                                "and ask whether the other one in April can be moved to the same week.",
                            hourOfDay = 21,
                            minute = 0,
                            createdAt = 1_772_900_000_000L,
                        ),
                    ),
                ),
            )
        },
    )
}
