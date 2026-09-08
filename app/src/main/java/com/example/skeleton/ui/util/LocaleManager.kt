package com.example.skeleton.ui.util

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

/**
 * Handles applying and reading the app locale. Persistence of the selected language
 * remains in [com.example.skeleton.domain.repository.SettingRepository]; this class
 * only applies the locale to the app and exposes the current application locale.
 *
 * Use this instead of calling [AppCompatDelegate.setApplicationLocales] directly.
 */
class LocaleManager {

    /**
     * Applies the given language code as the app locale. Triggers activity recreation.
     * Call after persisting the user's choice (e.g. via [SettingViewModel.setLanguage]).
     */
    fun applyLocale(languageCode: String) {
        val appLocale: LocaleListCompat = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    /**
     * Returns the current application locale language tag, or null if none is set.
     */
    fun getApplicationLanguageCode(): String? {
        val locales = AppCompatDelegate.getApplicationLocales()
        return locales[0]?.toLanguageTag()
    }
}
