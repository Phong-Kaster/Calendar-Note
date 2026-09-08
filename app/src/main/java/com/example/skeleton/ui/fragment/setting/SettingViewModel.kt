package com.example.skeleton.ui.fragment.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.common.Language
import com.example.skeleton.domain.repository.SettingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SettingViewModel(
    private val settingRepository: SettingRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingUiState())
    val uiState = _uiState.asStateFlow()

    val darkModeFlow = settingRepository.enableDarkModeFlow

    fun toggleDarkMode(value: Boolean) {
        viewModelScope.launch {
            settingRepository.setEnableDarkMode(value)
        }
    }


    init {
        observeLanguage()
    }


    private fun observeLanguage() {
        viewModelScope.launch {
            settingRepository.languageFlow.collectLatest { language ->
                _uiState.value = _uiState.value.copy(selectedLanguage = language)
            }
        }
    }


    fun setLanguage() {
        viewModelScope.launch {
            val selectedLanguage = _uiState.value.selectedLanguage
            settingRepository.setLanguage(selectedLanguage)
        }
    }

    fun changeLanguage(language: Language) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(selectedLanguage = language)
        }
    }
}
