package com.example.skeleton.ui.fragment.calendar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.fragment.findNavController
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.calendar.component.CalendarDayNotes
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthGrid
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthHeader
import com.example.skeleton.ui.fragment.note.NoteFragment
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.util.NavigationUtil.safeNavigate
import kotlinx.coroutines.delay
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

class CalendarFragment : CoreFragment() {
    private val viewModel: CalendarViewModel by viewModel()

    /**
     * Asks the ViewModel to check the date every time this screen comes back to the front.
     *
     * The screen can be built before midnight and looked at after it — a trip to the note editor
     * and back is enough. Without this the grid would keep calling yesterday "today" and would
     * draw the real today as a day still to come: dimmed and refusing taps.
     *
     * This is the half that catches a return to the screen. The `LaunchedEffect` in
     * [ComposeView] catches the other half — the day turning while the screen is simply sat in
     * front of the user — and both call the same function.
     */
    override fun onResume() {
        super.onResume()
        viewModel.refreshToday()
    }

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        // Waits for the day to actually turn, then asks the ViewModel to read the clock again.
        //
        // `onResume` above covers the common case — the screen outliving a trip to the note
        // editor — but not a rollover while the screen is sitting in front of the user,
        // untouched. In that window the grid keeps yesterday filled in as today and draws the
        // real today dimmed and refusing taps, and DoD criterion 9 says today is always
        // selectable.
        //
        // Keyed on `uiState.today`, so the effect is torn down and re-armed for the following
        // midnight the moment the day changes. The loop is what makes it safe when it is not:
        // if the wake-up lands a moment early and `refreshToday()` finds the date unchanged,
        // `today` does not change, the effect is not restarted, and a single `delay` would have
        // given up for good. `millisUntilNextDay()` never returns zero, so this cannot spin.
        LaunchedEffect(uiState.today) {
            while (true) {
                delay(timeMillis = viewModel.millisUntilNextDay())
                viewModel.refreshToday()
            }
        }

        CalendarLayout(
            uiState = uiState,
            onPreviousMonth = { viewModel.showPreviousMonth() },
            onNextMonth = { viewModel.showNextMonth() },
            onSelectDay = { date -> viewModel.selectDate(date = date) },
            onOpenNote = { note ->
                // The same route and the same guard Home uses, for the same reason: two taps
                // inside one moment must not push two copies of the editor onto the back stack,
                // and `safeNavigate` does not debounce — it only swallows exceptions.
                //
                // Asking the graph where we are, rather than `NavigationUtil.canNavigate()`:
                // that clock is one 800 ms field shared by the whole app and `CoreBottomBar`
                // arms it even for a tab tap that navigates nowhere, so tapping "Calendar"
                // while already on the Calendar screen would leave every note row dead for the
                // next 800 ms. See the same comment in `HomeFragment`.
                val currentDestination = runCatching {
                    findNavController().currentDestination?.id
                }.getOrNull()

                if (currentDestination == R.id.calendarFragment) {
                    safeNavigate(
                        destination = R.id.toNote,
                        // The note's own day, not the picked one. They are the same day today —
                        // the list only ever holds notes from the selected day — but the editor
                        // falls back to this argument when the note has gone missing, and the
                        // note's own date is the right thing to fall back to.
                        bundle = NoteFragment.argumentsFor(date = note.date, noteId = note.id),
                    )
                }
            },
            onCreateNote = {
                // Today, exactly as on Home and on Settings — the centre button means the same
                // thing everywhere in the app right now. Writing a note **on the day the user
                // picked here** is DoD criterion 11 and belongs to T-008, which adds an add-note
                // action of its own alongside that day's notes.
                //
                // A fresh `LocalDate.now()` and not `uiState.today`, even though the two are
                // normally equal and `refreshToday()` is what keeps them so. If the clock has
                // rolled over while this screen sat untouched, the state is a day behind — and
                // of the two ways to be inconsistent, a note filed on the wrong day is worse
                // than a grid that has not caught up yet.
                safeNavigate(
                    destination = R.id.toNote,
                    bundle = NoteFragment.argumentsFor(date = LocalDate.now()),
                )
            },
        )
    }
}

/**
 * The Calendar screen: one month at a time, with today marked, the future switched off, and the
 * picked day's notes underneath.
 *
 * Pure UI — it draws the state it is handed and navigates nowhere, which is what lets the
 * previews below render it with invented data and no database behind them.
 *
 * Both dates are turned into words **here** rather than in the ViewModel or in the components
 * below, because they are the only things on this screen that depend on which language the app
 * is showing. That is `LocalConfiguration`'s locale and not the device's: this app has its own
 * language picker in Settings, so `Locale.getDefault()` would answer for the phone and could
 * print an English month under German copy.
 *
 * @param uiState what to draw.
 * @param onPreviousMonth the user tapped the back arrow.
 * @param onNextMonth the user tapped the forward arrow.
 * @param onSelectDay the user tapped a day. Future days never reach this — they are not tappable.
 * @param onOpenNote the user tapped one of the selected day's notes.
 * @param onCreateNote the user tapped the bottom bar's centre action button.
 * @author Phong-Kaster
 */
@Composable
private fun CalendarLayout(
    uiState: CalendarUiState,
    onPreviousMonth: () -> Unit = {},
    onNextMonth: () -> Unit = {},
    onSelectDay: (LocalDate) -> Unit = {},
    onOpenNote: (Note) -> Unit = {},
    onCreateNote: () -> Unit = {},
) {
    val locale = LocalConfiguration.current.locales[0]

    // Remembered against the locale rather than rebuilt inline: every note written anywhere in
    // the app produces a fresh CalendarUiState, and each one would otherwise parse the pattern
    // string again. It changes only when the user changes the app's language.
    val monthFormatter = remember(locale) {
        DateTimeFormatter.ofPattern("MMMM yyyy").withLocale(locale)
    }

    // The picked day written out in full — "Thursday, 10 September 2026" — and written out
    // *here* rather than inside `CalendarDayNotes`, so that component takes a plain String and
    // can be photographed without the rendering machine's locale getting into the picture. A
    // localised style rather than a pattern, because word order and the position of the year
    // are not the same in every language and a hand-written pattern only ever fits one.
    val dayFormatter = remember(locale) {
        DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).withLocale(locale)
    }

    CoreLayout(
        modifier = Modifier,
        topBar = { CoreTopBar(title = stringResource(R.string.calendar)) },
        bottomBar = { CoreBottomBar(onCreateNote = onCreateNote) },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    // Six rows of squares plus a header do not fit on every phone, and a month
                    // with its last week off the bottom of the screen is a month the user cannot
                    // reach. The day's notes hang under all of it, which only makes it longer —
                    // so the grid and the list scroll together as one page rather than the list
                    // being given its own little window to scroll inside.
                    .verticalScroll(state = rememberScrollState())
                    .padding(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CalendarMonthHeader(
                    label = uiState.displayedMonth.format(monthFormatter),
                    onPrevious = onPreviousMonth,
                    onNext = onNextMonth,
                )

                CalendarMonthGrid(
                    days = uiState.days,
                    today = uiState.today,
                    displayedMonth = uiState.displayedMonth,
                    selectedDate = uiState.selectedDate,
                    datesWithNotes = uiState.datesWithNotes,
                    onDayClick = onSelectDay,
                )

                CalendarDayNotes(
                    dayLabel = uiState.selectedDate?.format(dayFormatter),
                    notes = uiState.notesForSelectedDay,
                    onOpenNote = onOpenNote,
                )
            }
        },
    )
}

@Preview(name = "Calendar - this month")
@Composable
private fun CalendarLayoutPreview() {
    MyApplicationTheme {
        CalendarLayout(
            uiState = CalendarUiState(
                today = LocalDate.of(2026, 9, 10),
                displayedMonth = YearMonth.of(2026, 9),
                selectedDate = LocalDate.of(2026, 9, 10),
                datesWithNotes = setOf(LocalDate.of(2026, 9, 3), LocalDate.of(2026, 9, 10)),
                loadedDay = LocalDate.of(2026, 9, 10),
                loadedDayNotes = listOf(
                    Note(
                        id = 1L,
                        date = LocalDate.of(2026, 9, 10),
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

@Preview(name = "Calendar - a day with nothing on it")
@Composable
private fun CalendarLayoutEmptyDayPreview() {
    // The whole page in the state the prior attempt at this screen got wrong: a day is picked,
    // it has no notes, and what sits under the grid has to say so rather than be blank.
    MyApplicationTheme {
        CalendarLayout(
            uiState = CalendarUiState(
                today = LocalDate.of(2026, 9, 10),
                displayedMonth = YearMonth.of(2026, 9),
                selectedDate = LocalDate.of(2026, 9, 7),
                datesWithNotes = setOf(LocalDate.of(2026, 9, 3), LocalDate.of(2026, 9, 10)),
            ),
        )
    }
}

@Preview(name = "Calendar - a month still to come")
@Composable
private fun CalendarLayoutFutureMonthPreview() {
    // Every square disabled, which is what paging forward looks like. Worth its own preview: it
    // is the state most likely to be mistaken for a broken screen if the dimming is ever wrong.
    MyApplicationTheme {
        CalendarLayout(
            uiState = CalendarUiState(
                today = LocalDate.of(2026, 9, 10),
                displayedMonth = YearMonth.of(2026, 10),
                selectedDate = LocalDate.of(2026, 9, 10),
            ),
        )
    }
}
