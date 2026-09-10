package com.example.skeleton.ui.fragment.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.fragment.findNavController
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.home.component.HomeNoteList
import com.example.skeleton.ui.fragment.home.component.HomeRequestPermission
import com.example.skeleton.ui.fragment.home.component.isNotificationGranted
import com.example.skeleton.ui.fragment.note.NoteFragment
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.util.NavigationUtil.safeNavigate
import com.example.skeleton.ui.util.PermissionUtil.isLocationGranted
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate

class HomeFragment : CoreFragment() {
    private val viewModel: HomeViewModel by viewModel()

    // Enable request permission
    var triggerRequestPermission by mutableIntStateOf(0)


    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    private fun checkPermission() {

        val notiEnable = isNotificationGranted(requireContext())
        val locationEnable = isLocationGranted(requireContext())

        if (!notiEnable) {
            triggerRequestPermission++
        }

        if (!locationEnable) {
            triggerRequestPermission++
        }
    }

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        HomeLayout(
            uiState = uiState,
            onCreateNote = {
                safeNavigate(
                    destination = R.id.toNote,
                    bundle = NoteFragment.argumentsFor(date = LocalDate.now()),
                )
            },
            onOpenNote = { note ->
                // Two taps landing inside the same moment must not push two copies of the editor
                // onto the back stack — leaving the note would then take two presses of back, and
                // `safeNavigate` does not debounce for itself; it only swallows exceptions.
                //
                // The instrument for that is *where we are*, not `NavigationUtil.canNavigate()`.
                // That clock is one 800 ms field shared by every control in the app, and
                // `CoreBottomBar` arms it even for a tab tap that navigates nowhere — so tapping
                // "Home" while already on Home would leave every row dead for the next 800 ms,
                // ignoring taps for a reason the user cannot see. Asking the graph has no dead
                // window at all: `navigate` moves `currentDestination` immediately, so the second
                // tap of a double tap is already standing on the editor and is refused, while a
                // deliberate tap a moment later still works.
                val currentDestination = runCatching {
                    findNavController().currentDestination?.id
                }.getOrNull()

                if (currentDestination == R.id.homeFragment) {
                    safeNavigate(
                        destination = R.id.toNote,
                        // The note's own day, not today. The editor prefers the stored note's date
                        // anyway; this argument is what a note that has gone missing falls back
                        // to, and today would be the wrong day to fall back to.
                        bundle = NoteFragment.argumentsFor(date = note.date, noteId = note.id),
                    )
                }
            },
        )

        // Request notification, location and exact alarm permissions
        HomeRequestPermission(
            enable = triggerRequestPermission,
            onNotificationGranted = {
                // Handle notification granted (e.g. refresh UI)
            },
            onLocationGranted = {
                // Handle location granted (e.g. refresh location-based data)
            },
            onExactAlarmGranted = {
                // Handle exact alarm granted (e.g. reschedule alarms)
            }
        )
    }
}

/**
 * Home: every note in the app, most recently touched first, or a message saying there are none.
 *
 * Pure UI — it renders the state it is handed and navigates nowhere, which is what lets the
 * previews at the bottom of this file draw it with made-up data and no database behind them.
 *
 * @param uiState what to draw.
 * @param onCreateNote the user tapped the bottom bar's centre action button.
 * @param onOpenNote the user tapped a row and wants that note opened.
 * @author Phong-Kaster
 */
@Composable
private fun HomeLayout(
    uiState: HomeUiState,
    onCreateNote: () -> Unit = {},
    onOpenNote: (Note) -> Unit = {},
) {
    CoreLayout(
        modifier = Modifier,
        showLoading = uiState.isLoading,
        topBar = { CoreTopBar(title = stringResource(R.string.home)) },
        bottomBar = { CoreBottomBar(onCreateNote = onCreateNote) },
        content = {
            HomeNoteList(
                notes = uiState.notes,
                onOpenNote = onOpenNote,
            )
        },
    )
}

@Preview(name = "Home - with notes")
@Composable
private fun HomeLayoutPreview() {
    MyApplicationTheme {
        HomeLayout(
            uiState = HomeUiState(
                isLoading = false,
                notes = listOf(
                    Note(
                        id = 1L,
                        date = LocalDate.of(2026, 3, 14),
                        title = "Groceries",
                        content = "Coffee, oat milk, the good bread from the corner shop.",
                        createdAt = 1_773_000_000_000L,
                        updatedAt = 1_773_000_000_000L,
                    ),
                ),
            ),
        )
    }
}

@Preview(name = "Home - empty")
@Composable
private fun HomeLayoutEmptyPreview() {
    MyApplicationTheme {
        HomeLayout(uiState = HomeUiState(isLoading = false))
    }
}
