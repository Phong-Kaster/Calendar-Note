package com.example.skeleton.data.repository.impl

import com.example.skeleton.common.Language
import com.example.skeleton.data.datastore.SettingDatastore
import com.example.skeleton.domain.repository.SettingRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class SettingRepositoryImpl(
    private val settingDatastore: SettingDatastore
) : SettingRepository {

    override val enableIntroFlow: Flow<Boolean>
        get() = settingDatastore.enableIntroFlow

    override val enableLanguageIntroFlow: Flow<Boolean>
        get() = settingDatastore.enableLanguageIntroFlow

    override val languageFlow: Flow<Language>
        get() = settingDatastore.languageFlow

    override val enableDarkModeFlow: Flow<Boolean>
        get() = settingDatastore.enableDarkModeFlow

    // A straight pass-through, like every other field here: the datastore already turns the stored
    // text into a `LocalDate`, so there is nothing left for this class to add. Transforming it a
    // second time here would only create somewhere else for the two to disagree.
    override val lastGreetedDateFlow: Flow<LocalDate?>
        get() = settingDatastore.lastGreetedDateFlow

    override suspend fun setEnableIntro(value: Boolean) =
        settingDatastore.setEnableIntro(value)

    override suspend fun setEnableLanguageIntro(value: Boolean) =
        settingDatastore.setEnableLanguageIntro(value)

    override suspend fun setLanguage(language: Language) =
        settingDatastore.setLanguage(language)

    override suspend fun setEnableDarkMode(value: Boolean) =
        settingDatastore.setEnableDarkMode(value)

    override suspend fun setLastGreetedDate(date: LocalDate) =
        settingDatastore.setLastGreetedDate(date = date)
}