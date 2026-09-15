package com.example.skeleton.domain.repository

import com.example.skeleton.common.Language
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface SettingRepository {

    val enableIntroFlow: Flow<Boolean>
    val enableLanguageIntroFlow: Flow<Boolean>
    val languageFlow: Flow<Language>
    val enableDarkModeFlow: Flow<Boolean>

    /**
     * The day the user was last greeted, or `null` when they never have been.
     *
     * `null` also covers "what was stored could not be read", because both answers lead to the same
     * decision — greet — and a caller that had to tell them apart would only be able to do something
     * worse with the difference. The greeting itself is decided by
     * `domain/greeting/GreetingDecision.kt`.
     */
    val lastGreetedDateFlow: Flow<LocalDate?>

    suspend fun setEnableIntro(value: Boolean)

    suspend fun setEnableLanguageIntro(value: Boolean)

    suspend fun setLanguage(language: Language)

    suspend fun setEnableDarkMode(value: Boolean)

    /**
     * Remembers that the user has now been greeted on [date], so that nothing greets them again
     * until the calendar turns over.
     *
     * @param date the day they were greeted — always the day the decision was made on, never a fresh
     *   reading of the clock, so that the two can never straddle midnight and disagree.
     */
    suspend fun setLastGreetedDate(date: LocalDate)
}
