package com.example.skeleton.ui.fragment.calendar

import com.example.skeleton.domain.model.CalendarMonth
import java.time.LocalDate

/**
 * UI state for the Calendar screen.
 *
 * @param currentMonth month currently shown on screen.
 * @param today real-world today, used to highlight the current day cell.
 * @author Phong-Kaster
 */
data class CalendarUiState(
    val currentMonth: CalendarMonth = CalendarMonth.current(),
    val today: LocalDate = LocalDate.now(),
)
