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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking


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
}
