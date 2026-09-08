package com.example.skeleton.ui.fragment.calendar

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the currently displayed month and lets the user step to the previous/next month.
 *
 * No repository dependency yet — a later task adds `noteRepository` so notes can be
 * shown alongside dates.
 *
 * @author Phong-Kaster
 */
class CalendarViewModel : ViewModel() {

    private val TAG = "CalendarViewModel"

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState = _uiState.asStateFlow()

    /** Moves the displayed month back by one. */
    fun previousMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.previous())
    }

    /** Moves the displayed month forward by one. */
    fun nextMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.next())
    }
}
