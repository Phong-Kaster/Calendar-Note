package com.example.skeleton.ui.fragment.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

/**
 * Keeps the Calendar screen supplied with a month to draw, a selection, and the set of days that
 * already have notes on them.
 *
 * Two sources feed it and they behave differently. The **notes store** is a subscription that
 * stays open, so writing or deleting a note anywhere in the app makes the markers on this grid
 * appear and disappear with nothing to remember to call. The **clock** is read when the screen is
 * created, whenever it comes back to the front, and once more when the day actually turns — all
 * three through [refreshToday]. Not continuously, which would move the ground under a user
 * mid-tap, and not once for ever, which is worse: see that function.
 *
 * @param noteRepository the notes store, injected by interface so a test can hand over a fake.
 * @param clock where "today" comes from. A constructor parameter and not a bare `LocalDate.now()`,
 *   because two of this class's promises — that the grid opens on the current month, and that a
 *   day after today can never become the selection — are only checkable if a test can decide what
 *   day it is.
 * @author Phong-Kaster
 */
class CalendarViewModel(
    private val noteRepository: NoteRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) : ViewModel() {

    private val TAG = "CalendarViewModel"

    private val _uiState = MutableStateFlow(initialCalendarState(clock = clock))
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        collectNotes()
    }

    /**
     * Subscribes to the store and keeps the "this day has notes" markers current.
     *
     * Only the days are kept, not the notes themselves: the grid draws a dot, and holding every
     * note in memory to decide whether to draw a dot would be carrying the whole notebook to
     * answer a yes/no question. The day's actual notes are read when a day is selected (T-007).
     */
    private fun collectNotes() {
        viewModelScope.launch {
            noteRepository.notesFlow.collectLatest { notes ->
                _uiState.value = _uiState.value.copy(
                    datesWithNotes = notes.map { note -> note.date }.toSet(),
                )
            }
        }
    }

    /**
     * Reads the clock again, because the day may have changed since the screen was built.
     *
     * **A calendar that decides once what "today" is, is wrong every night.** This ViewModel
     * outlives a trip to the note editor and a spell in the background, so without this a user
     * who opened the screen at 23:58 and came back at 00:05 would find yesterday still filled in
     * as today, and **the real today drawn dimmed and refusing taps** — DoD criterion 9 says
     * today is always selectable, and it would be false on the one screen that shows it.
     *
     * Two things follow the clock and one deliberately does not:
     *
     * - **The displayed month follows, but only if the user had not paged away.** Somebody
     *   looking at last March asked to look at last March; dragging them back to the present
     *   because midnight passed would be the screen overruling them.
     * - **The selection stays put — unless the clock went *backwards* and left it in the
     *   future.** A day the user picked stays picked: it was a real day yesterday and it is a
     *   real day now, just no longer today, and moving it would silently change which day their
     *   next note lands on. But the clock does not only move forward. Flying west, a manual
     *   correction, or an NTP step-back all make `LocalDate.now()` earlier than it was, and the
     *   day already picked can end up *after* the new today. That day is drawn with a
     *   full-strength selection ring on a square that is dimmed and refuses taps — a control
     *   that looks chosen and does nothing — so it is dropped instead.
     *
     * Called from the Fragment's `onResume` **and** from a timer the Fragment arms for the next
     * local midnight, so a rollover is caught whether the screen is being returned to or simply
     * sat in front of.
     */
    fun refreshToday() {
        val today = LocalDate.now(clock)
        if (today == _uiState.value.today) return

        val wasShowingTheCurrentMonth =
            _uiState.value.displayedMonth == YearMonth.from(_uiState.value.today)

        _uiState.value = _uiState.value.copy(
            today = today,
            displayedMonth =
                if (wasShowingTheCurrentMonth) YearMonth.from(today)
                else _uiState.value.displayedMonth,
            selectedDate = _uiState.value.selectedDate?.takeIf { picked -> !picked.isAfter(today) },
        )
    }

    /**
     * How long there is to wait before the local day turns, in milliseconds.
     *
     * The Fragment sleeps for this and then calls [refreshToday], which is what closes the one
     * window `onResume` cannot: a rollover while the screen is sitting in front of the user,
     * untouched. Without it the grid keeps yesterday filled in as today and draws the real today
     * dimmed and refusing taps — and DoD criterion 9 says today is always selectable.
     *
     * It lives here rather than in the Fragment for the same reason `today` does: it reads the
     * injected [clock], so a test can put the clock at 23:59 and check the answer instead of
     * waiting all evening to find out.
     *
     * Never negative, and never zero. A caller loops on this, so an answer of zero would be a
     * hot spin; [MINIMUM_WAIT_MILLIS] is the floor, which also absorbs the case where the clock
     * is a hair behind and the first wake-up finds the day has not quite turned.
     *
     * @return milliseconds until the next local midnight, at least [MINIMUM_WAIT_MILLIS].
     */
    fun millisUntilNextDay(): Long {
        val now = LocalDateTime.now(clock)
        val nextMidnight = LocalDate.now(clock).plusDays(1L).atStartOfDay()

        return Duration.between(now, nextMidnight).toMillis().coerceAtLeast(MINIMUM_WAIT_MILLIS)
    }

    /** Shows the month before the one on screen. The selection, if any, stays where it is. */
    fun showPreviousMonth() {
        _uiState.value = _uiState.value.copy(
            displayedMonth = _uiState.value.displayedMonth.minusMonths(1L),
        )
    }

    /**
     * Shows the month after the one on screen.
     *
     * Deliberately unbounded: the user may page forward into next year if they like. Every day
     * there is after today, so every square arrives disabled — which is the rule doing its job
     * rather than a screen refusing to move.
     */
    fun showNextMonth() {
        _uiState.value = _uiState.value.copy(
            displayedMonth = _uiState.value.displayedMonth.plusMonths(1L),
        )
    }

    /**
     * Picks a day, so the screen can show what is on it.
     *
     * A day after today is refused here as well as being untappable on the grid. That is not
     * belt-and-braces for its own sake: the grid's disabled cell is what the *user* runs into, and
     * this is what a future caller — a deep link, a "jump to date" control, a test — runs into. A
     * selection the app can never legally act on should not be representable in the state at all.
     *
     * @param date the day the user tapped.
     */
    fun selectDate(date: LocalDate) {
        if (date.isAfter(_uiState.value.today)) {
            Log.w(TAG, "selectDate refused: $date is after today")
            return
        }

        _uiState.value = _uiState.value.copy(selectedDate = date)
    }
}

/** The shortest the Calendar screen will ever wait before looking at the clock again. */
private const val MINIMUM_WAIT_MILLIS = 1_000L

/**
 * The state the screen opens on: the current month, with today already picked.
 *
 * Today is selected from the start so that the screen has something to show immediately rather
 * than an unexplained gap under the grid, and so the very first thing a user looks at is the
 * combination most likely to be drawn wrong — today *and* selected at once.
 *
 * A single read of the clock, not three. Reading it once per field is the mistake the notes store
 * documents at length: three reads can straddle midnight and produce a screen whose "today" and
 * whose displayed month disagree.
 *
 * @param clock where today comes from.
 * @author Phong-Kaster
 */
private fun initialCalendarState(clock: Clock): CalendarUiState {
    val today = LocalDate.now(clock)

    return CalendarUiState(
        today = today,
        displayedMonth = YearMonth.from(today),
        selectedDate = today,
    )
}
