package com.example.skeleton.domain.repository

import com.example.skeleton.common.Language
import kotlinx.coroutines.flow.Flow

interface SettingRepository {

    val enableIntroFlow: Flow<Boolean>
    val enableLanguageIntroFlow: Flow<Boolean>
    val languageFlow: Flow<Language>
    val enableDarkModeFlow: Flow<Boolean>

    suspend fun setEnableIntro(value: Boolean)

    suspend fun setEnableLanguageIntro(value: Boolean)

    suspend fun setLanguage(language: Language)

    suspend fun setEnableDarkMode(value: Boolean)
}
