package com.example.skeleton.ui.fragment.alarms

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Holds the Alarms screen's state.
 *
 * **It deliberately does nothing yet.** There is no alarms store to read from — no repository, no
 * DAO, not even an `Alarm` model — so this class has no dependencies and no collectors. It exists
 * because every screen in this app is a Fragment plus a ViewModel plus a UiState, and putting the
 * spine in now means the task that actually stores an alarm only has to add a collector rather than
 * reshape the screen.
 *
 * The `init { }` block is missing on purpose rather than present and empty: there is nothing named
 * to call from it, and an empty block invites somebody to inline a `viewModelScope.launch` there,
 * which `viewmodel-layer.md` forbids.
 *
 * @author Phong-Kaster
 */
class AlarmsViewModel : ViewModel() {

    private val TAG = "AlarmsViewModel"

    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()
}
