package com.example.skeleton.ui.fragment.alarms

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.repository.AlarmRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Holds the Alarms screen's state: every alarm in the store, kept current.
 *
 * It reads and never writes. Creating and editing an alarm both happen on the editor screen, so
 * there is nothing for this class to save — its only job is to keep [uiState] level with what the
 * store holds.
 *
 * **It subscribes rather than fetching**, and that difference is what makes the screen work at all.
 * `alarmsFlow` is a live stream: saving an alarm on the editor makes it emit again on its own, so by
 * the time the user is back here the list already contains what they just wrote. A one-shot read in
 * `init` would have loaded the list once, at first construction, and then shown a stale list for as
 * long as the ViewModel lived — the user would write an alarm, come back, and see nothing.
 *
 * Nothing here sorts, filters or counts. The order is the store's promise (earliest time of day
 * first) and "is the screen empty?" is a computed `val` on [AlarmsUiState]; doing either job twice
 * would mean two places to keep right.
 *
 * @param alarmRepository the alarms store, injected by interface so a test can hand over a fake.
 * @author Phong-Kaster
 */
class AlarmsViewModel(
    private val alarmRepository: AlarmRepository,
) : ViewModel() {

    private val TAG = "AlarmsViewModel"

    private val _uiState = MutableStateFlow(AlarmsUiState())
    val uiState: StateFlow<AlarmsUiState> = _uiState.asStateFlow()

    init {
        collectAlarms()
    }

    /**
     * Keeps [uiState] level with the store, for as long as this ViewModel lives.
     *
     * `collectLatest` and not `collect`: only the newest list matters here. If two emissions land in
     * the same instant — a save landing while a previous list is still being copied into state — the
     * older one has nothing left to say, and drawing it first would be one frame of a list the store
     * has already replaced.
     *
     * There is no error branch, and there does not need to be one. The store's flow is fail-soft: a
     * database that will not read emits an empty list and logs, rather than throwing up into this
     * collector, which catches nothing. See `AlarmRepositoryImpl.alarmsFlow`.
     */
    private fun collectAlarms() {
        viewModelScope.launch {
            alarmRepository.alarmsFlow.collectLatest { alarms ->
                _uiState.value = _uiState.value.copy(alarms = alarms)
            }
        }
    }
}
