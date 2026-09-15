package com.example.skeleton.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.skeleton.MainApplication
import com.example.skeleton.common.Constant
import com.example.skeleton.common.Language
import com.example.skeleton.domain.greeting.formatGreetedDate
import com.example.skeleton.domain.greeting.parseGreetedDate

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.time.LocalDate


val Context.settingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = Constant.SETTING_DATASTORE
)

class SettingDatastore(
    context: Context
) {

    private val datastore = context.settingDataStore

    private val languageKey = stringPreferencesKey("languageKey")
    private val enableIntroKey = booleanPreferencesKey("enableIntroKey")
    private val enableLanguageIntroKey = booleanPreferencesKey("enableLanguageIntroKey")
    private val enableDarkModeKey = booleanPreferencesKey("enableDarkModeKey")
    private val lastGreetedDateKey = stringPreferencesKey("lastGreetedDateKey")

    // ---------- Enable Intro ----------
    val enableIntroFlow: Flow<Boolean> =
        datastore.data.map { it[enableIntroKey] ?: true }

    suspend fun setEnableIntro(value: Boolean) {
        datastore.edit { it[enableIntroKey] = value }
    }

    // ---------- Enable Language Intro ----------
    val enableLanguageIntroFlow: Flow<Boolean> =
        datastore.data.map { it[enableLanguageIntroKey] ?: true }

    suspend fun setEnableLanguageIntro(value: Boolean) {
        datastore.edit { it[enableLanguageIntroKey] = value }
    }

    // ---------- Language ----------
    val languageFlow: Flow<Language> =
        datastore.data.map {
            Language.getByCode(it[languageKey])
        }

    suspend fun setLanguage(language: Language) {
        datastore.edit {
            it[languageKey] = language.code
        }
    }

    // ---------- Dark Mode ----------
    val enableDarkModeFlow: Flow<Boolean> =
        datastore.data.map { it[enableDarkModeKey] ?: true }

    suspend fun setEnableDarkMode(value: Boolean) {
        datastore.edit { it[enableDarkModeKey] = value }
    }

    // ---------- Last Greeted Date ----------
    /*
     * --- Why the day the user was last greeted lives on disk (simple story) ---
     *
     * The app says hello once per calendar day, the first time it comes to the front. "Have we
     * already said hello today?" therefore has to survive the app being closed, swiped away or killed
     * by the system — a field on a class would forget it the moment the process died, and the user
     * would be greeted again and again all day long.
     *
     * It is stored as **text** (`"2026-03-14"`, ISO-8601) rather than a number of milliseconds,
     * because the question is about a *day* and not an instant. The pair of functions that write and
     * read that text live in `domain/greeting/GreetingDecision.kt` so that the format is decided in
     * one place and can be tested — mapping a stored value into a proper type right here is the same
     * thing `languageFlow` does with `Language.getByCode`.
     *
     * `null` means "we do not know": nobody has ever been greeted, or what was stored could not be
     * read. Both lead to a greeting, which is the safe way round.
     */
    val lastGreetedDateFlow: Flow<LocalDate?> =
        datastore.data.map { parseGreetedDate(stored = it[lastGreetedDateKey]) }

    suspend fun setLastGreetedDate(date: LocalDate) {
        datastore.edit { it[lastGreetedDateKey] = formatGreetedDate(date = date) }
    }
}
