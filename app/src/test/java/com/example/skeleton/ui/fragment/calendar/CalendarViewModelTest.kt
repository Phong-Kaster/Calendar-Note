package com.example.skeleton.ui.fragment.calendar

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneOffset

/**
 * The Calendar screen's logic, held to what it promises without rendering anything.
 *
 * Three things live here that no picture can show: that the screen opens on the right month, that
 * paging around does not lose the user's selection, and that a day after today can never become
 * the selection **even for a caller that does not go through the grid**. The grid's disabled
 * square is what a person runs into; this is what a deep link, a future "jump to date" control,
 * or a careless refactor runs into.
 *
 * The clock is injected and fixed, so "today" is 10 September 2026 for the whole file. A test that
 * read the real clock would pass all year and fail on the last day of a month.
 *
 * `Dispatchers.setMain` is what makes a ViewModel testable off a device at all: `viewModelScope`
 * posts to `Dispatchers.Main`, which does not exist here. With an unconfined test dispatcher every
 * `launch` runs where it is started, so a test can assert on the next line.
 *
 * @author Phong-Kaster
 */
@OptIn(ExperimentalCoroutinesApi::class)
class CalendarViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun replaceMainDispatcher() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun restoreMainDispatcher() {
        Dispatchers.resetMain()
    }

    // ---------- Where the screen opens ----------

    @Test
    fun `the screen opens on the month containing today, with today picked`() {
        val viewModel = CalendarViewModel(
            noteRepository = FakeNoteRepository(),
            clock = fixedClockAt(TODAY),
        )

        assertEquals(TODAY, viewModel.uiState.value.today)
        assertEquals(YearMonth.of(2026, 9), viewModel.uiState.value.displayedMonth)
        assertEquals(TODAY, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `today comes from the clock it was given and nowhere else`() {
        // A second, deliberately absurd date. If anything in the chain reached for
        // `LocalDate.now()` instead of the injected clock, this is the test that says so — the
        // one above would keep passing on the machine that wrote it.
        val viewModel = CalendarViewModel(
            noteRepository = FakeNoteRepository(),
            clock = fixedClockAt(LocalDate.of(2019, 12, 31)),
        )

        assertEquals(LocalDate.of(2019, 12, 31), viewModel.uiState.value.today)
        assertEquals(YearMonth.of(2019, 12), viewModel.uiState.value.displayedMonth)
    }

    @Test
    fun `the grid it opens with is the displayed month's own`() {
        val viewModel = CalendarViewModel(
            noteRepository = FakeNoteRepository(),
            clock = fixedClockAt(TODAY),
        )

        val days = viewModel.uiState.value.days

        assertEquals(35, days.size)
        assertTrue(days.contains(TODAY))
        assertTrue(days.contains(LocalDate.of(2026, 9, 30)))
    }

    // ---------- Moving between months ----------

    @Test
    fun `paging back shows the month before`() {
        val viewModel = calendarViewModel()

        viewModel.showPreviousMonth()

        assertEquals(YearMonth.of(2026, 8), viewModel.uiState.value.displayedMonth)
        // The grid follows the month. Derived rather than stored precisely so it cannot lag —
        // a label saying August over September's squares is the failure being ruled out.
        assertEquals(LocalDate.of(2026, 8, 1), viewModel.uiState.value.days.filterNotNull().first())
    }

    @Test
    fun `paging forward shows the month after`() {
        val viewModel = calendarViewModel()

        viewModel.showNextMonth()

        assertEquals(YearMonth.of(2026, 10), viewModel.uiState.value.displayedMonth)
        assertEquals(
            LocalDate.of(2026, 10, 1),
            viewModel.uiState.value.days.filterNotNull().first(),
        )
    }

    @Test
    fun `paging over a year boundary keeps the year straight`() {
        val viewModel = calendarViewModel()

        repeat(times = 4) { viewModel.showNextMonth() }

        assertEquals(YearMonth.of(2027, 1), viewModel.uiState.value.displayedMonth)
    }

    @Test
    fun `paging does not move today`() {
        // A guard against one specific future slip, named so nobody mistakes it for a claim
        // about behaviour that is hard to get wrong: writing `copy(displayedMonth = …, today =
        // …)` in the paging functions, so that whichever month the user is looking at starts
        // rendering as the present one — every square in last March drawn as a live day.
        val viewModel = calendarViewModel()

        viewModel.showNextMonth()
        viewModel.showNextMonth()
        viewModel.showPreviousMonth()

        assertEquals(TODAY, viewModel.uiState.value.today)
    }

    // ---------- Midnight ----------

    @Test
    fun `coming back after midnight moves today onto the new day`() {
        // **A calendar that decides once what today is, is wrong every night.** The ViewModel
        // outlives a trip to the note editor, so without this the screen would keep yesterday
        // filled in as today and draw the real today dimmed and refusing taps — DoD criterion 9
        // says today is always selectable, and this is the screen that would make it false.
        val clock = MovableClock(today = TODAY)
        val viewModel = CalendarViewModel(noteRepository = FakeNoteRepository(), clock = clock)

        clock.today = TODAY.plusDays(1L)
        viewModel.refreshToday()

        assertEquals(TODAY.plusDays(1L), viewModel.uiState.value.today)
    }

    @Test
    fun `after the clock rolls over the new today can be picked and the old one still can`() {
        // Both halves, because the name used to promise both and only check one. The day that
        // was refused as "tomorrow" a minute ago has to become pickable, and the day that was
        // today must not become anything special on its way into the past.
        val clock = MovableClock(today = TODAY)
        val viewModel = CalendarViewModel(noteRepository = FakeNoteRepository(), clock = clock)

        clock.today = TODAY.plusDays(1L)
        viewModel.refreshToday()

        viewModel.selectDate(date = TODAY.plusDays(1L))
        assertEquals(TODAY.plusDays(1L), viewModel.uiState.value.selectedDate)

        viewModel.selectDate(date = TODAY)
        assertEquals(TODAY, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `crossing into a new month carries the page with it`() {
        // The user was looking at the current month, so they still are.
        val clock = MovableClock(today = LocalDate.of(2026, 9, 30))
        val viewModel = CalendarViewModel(noteRepository = FakeNoteRepository(), clock = clock)
        assertEquals(YearMonth.of(2026, 9), viewModel.uiState.value.displayedMonth)

        clock.today = LocalDate.of(2026, 10, 1)
        viewModel.refreshToday()

        assertEquals(YearMonth.of(2026, 10), viewModel.uiState.value.displayedMonth)
    }

    @Test
    fun `a user who paged away is not dragged back by midnight`() {
        // Somebody reading last March asked to read last March. Following the clock here would
        // be the screen overruling them for a reason they cannot see.
        val clock = MovableClock(today = LocalDate.of(2026, 9, 30))
        val viewModel = CalendarViewModel(noteRepository = FakeNoteRepository(), clock = clock)
        viewModel.showPreviousMonth()

        clock.today = LocalDate.of(2026, 10, 1)
        viewModel.refreshToday()

        assertEquals(YearMonth.of(2026, 8), viewModel.uiState.value.displayedMonth)
        // Today still moved — it is only the page that stayed put.
        assertEquals(LocalDate.of(2026, 10, 1), viewModel.uiState.value.today)
    }

    @Test
    fun `midnight does not move the day the user picked`() {
        // A picked day is a choice about a day, not about "today". Moving it would silently
        // change which day the user's next note lands on.
        val clock = MovableClock(today = TODAY)
        val viewModel = CalendarViewModel(noteRepository = FakeNoteRepository(), clock = clock)
        viewModel.selectDate(date = LocalDate.of(2026, 9, 3))

        clock.today = TODAY.plusDays(1L)
        viewModel.refreshToday()

        assertEquals(LocalDate.of(2026, 9, 3), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `a clock that goes backwards drops a selection it has overtaken`() {
        // The clock does not only move forward. Fly west, correct the date by hand, or take an
        // NTP step back, and `today` becomes *earlier* than it was — leaving the day the user
        // picked stranded in the future. That square would be drawn with a full-strength
        // selection ring while being dimmed and refusing taps: a control that looks chosen and
        // does nothing.
        val clock = MovableClock(today = TODAY)
        val viewModel = CalendarViewModel(noteRepository = FakeNoteRepository(), clock = clock)
        viewModel.selectDate(date = TODAY)

        clock.today = TODAY.minusDays(3L)
        viewModel.refreshToday()

        assertEquals(null, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `a clock that goes backwards keeps a selection it has not overtaken`() {
        // The other side of the same line, and the one with teeth: the fix must drop *only* the
        // days that are now in the future. Clearing the selection whenever the clock moves back
        // would also pass the test above, and would blank the screen for a user whose phone
        // merely re-synced.
        val clock = MovableClock(today = TODAY)
        val viewModel = CalendarViewModel(noteRepository = FakeNoteRepository(), clock = clock)
        viewModel.selectDate(date = LocalDate.of(2026, 9, 3))

        clock.today = LocalDate.of(2026, 9, 5)
        viewModel.refreshToday()

        assertEquals(LocalDate.of(2026, 9, 3), viewModel.uiState.value.selectedDate)
    }

    // ---------- Waiting for the day to turn ----------

    @Test
    fun `the wait to the next day is measured from the clock, not guessed`() {
        // The screen sleeps for this and then re-reads the date, which is what catches midnight
        // passing while the user is simply looking at the grid. At 21:00 there are three hours
        // to go, and a hardcoded interval — an hour, a day — is exactly the thing this asserts
        // against.
        val viewModel = CalendarViewModel(
            noteRepository = FakeNoteRepository(),
            clock = clockAt(dateTime = TODAY.atTime(21, 0)),
        )

        assertEquals(3L * 60L * 60L * 1_000L, viewModel.millisUntilNextDay())
    }

    @Test
    fun `the wait just before midnight is a minute, not a day`() {
        // The off-by-a-day that would make the whole mechanism useless: computing the distance
        // to midnight *today* rather than to the next one gives a negative number here, and a
        // stray `abs` or a day's offset would turn a one-minute wait into a 24-hour one.
        val viewModel = CalendarViewModel(
            noteRepository = FakeNoteRepository(),
            clock = clockAt(dateTime = TODAY.atTime(23, 59)),
        )

        assertEquals(60L * 1_000L, viewModel.millisUntilNextDay())
    }

    @Test
    fun `the wait is never zero, so a caller looping on it cannot spin`() {
        // Exactly midnight: the honest answer is a whole day, but the case that matters is the
        // floor. The Fragment loops on this value, so a zero would be a hot loop on the main
        // thread rather than a screen waiting for tomorrow.
        val viewModel = CalendarViewModel(
            noteRepository = FakeNoteRepository(),
            clock = clockAt(dateTime = TODAY.atStartOfDay()),
        )

        assertTrue(viewModel.millisUntilNextDay() > 0L)
    }

    @Test
    fun `resuming on the same day changes nothing`() {
        // `onResume` fires on every return to the screen, which is far more often than midnight
        // passes. It must not quietly undo the month the user paged to.
        val viewModel = calendarViewModel()
        viewModel.showPreviousMonth()
        viewModel.selectDate(date = LocalDate.of(2026, 9, 3))

        viewModel.refreshToday()

        assertEquals(YearMonth.of(2026, 8), viewModel.uiState.value.displayedMonth)
        assertEquals(LocalDate.of(2026, 9, 3), viewModel.uiState.value.selectedDate)
        assertEquals(TODAY, viewModel.uiState.value.today)
    }

    @Test
    fun `paging away does not throw the selection away`() {
        // Deliberately selects a day that is *not* the one the screen opened on, so the
        // assertion cannot pass by accident against a screen that simply never changed.
        val viewModel = calendarViewModel()
        viewModel.selectDate(date = LocalDate.of(2026, 9, 3))

        viewModel.showPreviousMonth()

        assertEquals(LocalDate.of(2026, 9, 3), viewModel.uiState.value.selectedDate)
    }

    // ---------- Picking a day ----------

    @Test
    fun `a past day can be picked`() {
        val viewModel = calendarViewModel()

        viewModel.selectDate(date = LocalDate.of(2026, 9, 3))

        assertEquals(LocalDate.of(2026, 9, 3), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `today can be picked`() {
        // Picked *back*, having been moved away first — asserting that today is selected on a
        // screen that already selects today at startup would be asserting the default.
        val viewModel = calendarViewModel()
        viewModel.selectDate(date = LocalDate.of(2026, 9, 3))

        viewModel.selectDate(date = TODAY)

        assertEquals(TODAY, viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `a day after today cannot be picked, and the old selection stays`() {
        // DoD criterion 9, below the grid. The second assertion is the one with teeth: refusing
        // by clearing the selection would also "not select tomorrow", and would blank the screen
        // every time a user's thumb landed on a greyed-out square.
        val viewModel = calendarViewModel()
        viewModel.selectDate(date = LocalDate.of(2026, 9, 3))

        viewModel.selectDate(date = TODAY.plusDays(1L))

        assertEquals(LocalDate.of(2026, 9, 3), viewModel.uiState.value.selectedDate)
    }

    @Test
    fun `no day of a month still to come can be picked`() {
        // The selection is moved off the screen's own default first. Asserting that it is still
        // TODAY after the loop would otherwise pass against a `selectDate` that does nothing at
        // all — the default is TODAY, so the expected value would already be sitting there.
        val viewModel = calendarViewModel()
        viewModel.selectDate(date = LocalDate.of(2026, 9, 3))

        viewModel.showNextMonth()
        (1..31).forEach { dayOfMonth ->
            viewModel.selectDate(date = LocalDate.of(2026, 10, dayOfMonth))
        }

        assertEquals(LocalDate.of(2026, 9, 3), viewModel.uiState.value.selectedDate)
    }

    // ---------- The has-notes markers ----------

    @Test
    fun `the days carrying notes come from the store`() {
        val repository = FakeNoteRepository(
            initialNotes = listOf(
                noteOn(date = LocalDate.of(2026, 9, 3)),
                noteOn(date = LocalDate.of(2026, 9, 8)),
            ),
        )

        val viewModel = CalendarViewModel(noteRepository = repository, clock = fixedClockAt(TODAY))

        assertEquals(
            setOf(LocalDate.of(2026, 9, 3), LocalDate.of(2026, 9, 8)),
            viewModel.uiState.value.datesWithNotes,
        )
    }

    @Test
    fun `several notes on one day mark that day once`() {
        // A shape check, and labelled as one. De-duplication here is done by the *type* —
        // `datesWithNotes` is a `Set` — so no mutation of `collectNotes` can make this fail that
        // would not also fail the test above it. It is kept because the type is the mechanism:
        // if that field is ever widened to a `List`, the grid starts drawing one dot per note on
        // top of itself, and this is the line that says the `Set` was load-bearing.
        val repository = FakeNoteRepository(
            initialNotes = listOf(
                noteOn(date = LocalDate.of(2026, 9, 3), id = 1L),
                noteOn(date = LocalDate.of(2026, 9, 3), id = 2L),
                noteOn(date = LocalDate.of(2026, 9, 3), id = 3L),
            ),
        )

        val viewModel = CalendarViewModel(noteRepository = repository, clock = fixedClockAt(TODAY))

        assertEquals(setOf(LocalDate.of(2026, 9, 3)), viewModel.uiState.value.datesWithNotes)
    }

    @Test
    fun `a note written while the screen is open makes its day light up`() {
        // The subscription, not the first read. A one-shot load would pass every test above and
        // leave the grid frozen the moment the user wrote something.
        val repository = FakeNoteRepository()
        val viewModel = CalendarViewModel(noteRepository = repository, clock = fixedClockAt(TODAY))
        assertEquals(emptySet<LocalDate>(), viewModel.uiState.value.datesWithNotes)

        repository.emit(notes = listOf(noteOn(date = LocalDate.of(2026, 9, 8))))

        assertEquals(setOf(LocalDate.of(2026, 9, 8)), viewModel.uiState.value.datesWithNotes)
    }

    @Test
    fun `deleting the last note on a day puts its marker out`() {
        val repository =
            FakeNoteRepository(initialNotes = listOf(noteOn(date = LocalDate.of(2026, 9, 8))))
        val viewModel = CalendarViewModel(noteRepository = repository, clock = fixedClockAt(TODAY))
        assertEquals(setOf(LocalDate.of(2026, 9, 8)), viewModel.uiState.value.datesWithNotes)

        repository.emit(notes = emptyList())

        assertEquals(emptySet<LocalDate>(), viewModel.uiState.value.datesWithNotes)
    }

    // ---------- Helpers ----------

    /** A view model on an empty store, with the clock fixed at [TODAY]. */
    private fun calendarViewModel(): CalendarViewModel = CalendarViewModel(
        noteRepository = FakeNoteRepository(),
        clock = fixedClockAt(TODAY),
    )

    private companion object {

        /** Thursday 10 September 2026, comfortably inside its month. */
        private val TODAY: LocalDate = LocalDate.of(2026, 9, 10)

        /** A clock that says it is midnight on [date], for ever. */
        private fun fixedClockAt(date: LocalDate): Clock = Clock.fixed(
            date.atStartOfDay(ZoneOffset.UTC).toInstant(),
            ZoneOffset.UTC,
        )

        /**
         * A clock stopped at a particular time of day, for the tests that care what o'clock it
         * is rather than only what date it is.
         */
        private fun clockAt(dateTime: LocalDateTime): Clock = Clock.fixed(
            dateTime.toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC,
        )

        /**
         * A clock whose day a test can push forward, so midnight can be crossed on demand.
         *
         * [Clock.fixed] cannot do this — it is fixed — and the screen's whole midnight problem
         * is about a second reading disagreeing with the first.
         */
        private class MovableClock(var today: LocalDate) : Clock() {

            override fun getZone(): java.time.ZoneId = ZoneOffset.UTC

            override fun withZone(zone: java.time.ZoneId): Clock = this

            override fun instant(): java.time.Instant =
                today.atStartOfDay(ZoneOffset.UTC).toInstant()
        }

        /** A stored note on [date]. Only its date is ever read by this screen. */
        private fun noteOn(date: LocalDate, id: Long = 1L): Note = Note(
            id = id,
            date = date,
            title = "Groceries",
            content = "Coffee, oat milk",
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )
    }
}

/**
 * A notes store whose list of notes a test can change while the screen is watching.
 *
 * The flow is a [MutableStateFlow] rather than a `flowOf(...)`, and that is the point: the
 * Calendar screen's markers are a *subscription*, so at least one test has to be able to push a
 * second list at it after the ViewModel has already been built.
 *
 * @param initialNotes the list the store starts out holding.
 * @author Phong-Kaster
 */
private class FakeNoteRepository(
    initialNotes: List<Note> = emptyList(),
) : NoteRepository {

    private val storedNotes = MutableStateFlow(initialNotes)

    override val notesFlow: Flow<List<Note>> = storedNotes

    /** Replaces what the store holds, as a write elsewhere in the app would. */
    fun emit(notes: List<Note>) {
        storedNotes.value = notes
    }

    override suspend fun getNote(id: Long): Outcome<Note?> =
        Outcome.Success(storedNotes.value.firstOrNull { note -> note.id == id })

    override suspend fun save(note: Note): Outcome<Unit> = Outcome.Success(Unit)

    override suspend fun delete(note: Note): Outcome<Unit> = Outcome.Success(Unit)
}
