package com.example.skeleton.ui.fragment.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Keeps the Home screen supplied with the list of notes.
 *
 * There is only one job here, and it is a subscription rather than a fetch: the store's flow stays
 * open for the life of the screen and pushes a new list whenever the `notes` table changes, so
 * writing a note anywhere in the app makes Home redraw itself with no refresh action and nothing
 * to remember to call.
 *
 * @param noteRepository the notes store, injected by interface so a test can hand over a fake.
 * @author Phong-Kaster
 */
class HomeViewModel(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        collectNotes()
    }

    /** Subscribes to the store and mirrors every list it sends into the UI state. */
    private fun collectNotes() {
        viewModelScope.launch {
            noteRepository.notesFlow.collectLatest { notes ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    notes = notes,
                )
            }
        }
    }
}
