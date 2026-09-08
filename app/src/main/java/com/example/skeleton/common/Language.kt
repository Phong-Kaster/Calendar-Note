package com.example.skeleton.common

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.skeleton.R
import java.util.Locale

/**
 * [Can get more country icon here](https://uxwing.com/?)
 */
enum class Language(
    val code: String,
    @StringRes val displayText: Int,
    @DrawableRes val drawable: Int,
) {
    English(code = "en", displayText = R.string.english, drawable = R.drawable.ic_language_english),
    German(code = "de", displayText = R.string.german, drawable = R.drawable.ic_language_german),
    French(code = "fr", displayText = R.string.french, drawable = R.drawable.ic_language_french),
    Hindi(code = "hi", displayText = R.string.hindi, drawable = R.drawable.ic_language_hindi),
    Japanese(code = "ja", displayText = R.string.japanese, drawable = R.drawable.ic_language_japanese),
    Korean(code = "ko", displayText = R.string.korean, drawable = R.drawable.ic_language_korean),
    Vietnam(code = "vi", displayText = R.string.vietnamese, drawable = R.drawable.ic_language_vietnamese),
    ;

    companion object {
        fun default(): Language = English

        fun getByCode(code: String?): Language {
            if (code == null) return default()
            return entries.firstOrNull { it.code == code } ?: English
        }

        fun getSortedList(): List<Language> {
            val list = entries.toMutableList()
            val defaultCode = Locale.getDefault().language
            val indexOfDefault = list.indexOfFirst { defaultCode == it.code }
            if (indexOfDefault > 0) {
                val item = list.removeAt(indexOfDefault)
                list.add(0, item)
            }
            return list
        }

        /**
         * put language of device to the top of list
         */
        fun generateListLanguage(): List<Language> {
            val supportedLanguages = Language.entries

            val deviceLanguage = Locale.getDefault().language
            val isLanguageSupported = deviceLanguage in supportedLanguages.map { it.code }
            val isDeviceLanguageEnglish = deviceLanguage == English.code
            val outcome = mutableListOf<Language>()


            if (isLanguageSupported) {
                val defaultLanguage = supportedLanguages.find { it.code == deviceLanguage } ?: English
                outcome.add(defaultLanguage)
            }


            for (language in supportedLanguages) {
                if (language == English && isDeviceLanguageEnglish) {
                    continue
                } else if (language.code == deviceLanguage) {
                    continue
                } else {
                    outcome.add(language)
                }
            }

            return outcome
        }
    }
}