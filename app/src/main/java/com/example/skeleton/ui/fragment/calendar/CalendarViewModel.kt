package com.example.skeleton.ui.fragment.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
        collectNotesForSelectedDay()
    }

    /**
     * Subscribes to the store and keeps the "this day has notes" markers current.
     *
     * Only the days are *kept*, not the notes themselves — the grid draws a dot, and there is no
     * reason to hold a whole notebook in memory to answer a yes/no question per square. Be
     * precise about what that does and does not save, though: `notesFlow` is backed by
     * `SELECT *`, so every note is still read and mapped on every write anywhere in the app, and
     * only the retention is avoided. Making that sentence fully true would mean a
     * `SELECT DISTINCT date` query of its own, which is a change to make when a table large
     * enough to notice exists — not before.
     *
     * The day's actual notes come from [collectNotesForSelectedDay], which asks the store for
     * one day rather than filtering this list.
     *
     * **These markers come from the store and never from the selection.** Picking a day must not
     * put a dot on it — the dot means "something is written here", and a dot that appears merely
     * because you looked at a day is a lie the user cannot check without opening it.
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
     * Keeps the list under the grid showing whatever is written on the day that is picked.
     *
     * **Two subscriptions in one, and the nesting is the interesting part.** The outer one
     * watches the *selection*; every time it changes, [flatMapLatest] throws away the previous
     * day's subscription and opens one on the new day. So the list follows the user's taps, and
     * it also follows the store — a note written, edited or deleted on the picked day updates
     * this list with nobody having to ask, exactly as the markers above do.
     *
     * `distinctUntilChanged` is what stops it re-subscribing for nothing: this collector writes
     * back into `_uiState`, so without it every unrelated state change — a month page, a new
     * marker, the list it just wrote itself — would tear the day's subscription down and build it
     * again. It cannot loop, because the field it writes is not the field it watches.
     *
     * **Nothing picked is its own case, and it is reachable.** `selectedDate` starts as today,
     * but `refreshToday` drops a selection that a backwards clock has left in the future. An
     * empty list is the right answer there; what must not happen is the *previous* day's notes
     * staying on screen under a grid with nothing selected.
     *
     * **The day is written down beside its notes, and that is what keeps the screen honest.**
     * The tap moves `selectedDate` immediately; the store's answer for the new day arrives a
     * query later. Storing only the list would leave a window where the heading names one day
     * and the rows below belong to another — so the list is stored with the day it is an answer
     * about, and `CalendarUiState.notesForSelectedDay` hands it to the screen only while the two
     * still agree.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun collectNotesForSelectedDay() {
        viewModelScope.launch {
            _uiState
                .map { state -> state.selectedDate }
                .distinctUntilChanged()
                .flatMapLatest { date ->
                    if (date == null) flowOf<Pair<LocalDate?, List<Note>>>(null to emptyList())
                    else noteRepository.notesForDateFlow(date = date)
                        .map { notes -> date to notes }
                }
                .collectLatest { (date, notes) ->
                    _uiState.value = _uiState.value.copy(
                        loadedDay = date,
                        loadedDayNotes = notes,
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

    /**
     * The day a note started from this screen belongs to: **the day the user picked.**
     *
     * DoD criterion 11 — the selected day, not today. It lives in the ViewModel rather than in the
     * Fragment's navigation lambda because a Fragment cannot be unit-tested on this toolchain, and
     * a decision carrying a criterion should not be the one place no test can reach.
     *
     * Two deliberate choices, argued out in `.ai/TASKS/T-008.md`:
     *
     * - **A picked day is handed over as it is, even if the clock has since moved backwards and
     *   left it in the future.** Swapping it for today would file the note on a day nobody chose,
     *   invisibly; re-checking the rule here would put a second copy of it above the repository,
     *   where `knowledge/DOMAIN.md` says it must not live. So the store refuses it and the editor
     *   names the day.
     * - **Nothing picked falls back to today, read fresh from the clock** rather than from
     *   `uiState.today`, which can be a day behind on a screen left open across midnight. Only the
     *   bottom bar's centre button can arrive here in that state; the action beside the day's
     *   notes is not drawn without a day to name.
     *
     * @return the day to open the note editor on.
     */
    fun dateForNewNote(): LocalDate = _uiState.value.selectedDate ?: LocalDate.now(clock)
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
