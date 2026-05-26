package com.codestream.tetrak.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object AppSettings {
    private const val PREFS = "tetrak_settings"
    private const val KEY_THEME = "theme_mode"
    private const val KEY_LANGUAGE = "language_tag"

    const val THEME_SYSTEM = "system"
    const val THEME_LIGHT = "light"
    const val THEME_DARK = "dark"

    const val LANGUAGE_SYSTEM = "system"
    const val LANGUAGE_ENGLISH = "en"
    const val LANGUAGE_ARMENIAN = "hy"
    const val LANGUAGE_RUSSIAN = "ru"
    const val LANGUAGE_ARABIC = "ar"
    const val LANGUAGE_PERSIAN = "fa"

    fun applySavedSettings(context: Context) {
        applyTheme(getTheme(context))
        applyLanguage(getLanguage(context))
    }

    fun getTheme(context: Context): String = prefs(context).getString(KEY_THEME, THEME_SYSTEM) ?: THEME_SYSTEM

    fun setTheme(context: Context, mode: String) {
        prefs(context).edit().putString(KEY_THEME, mode).apply()
        applyTheme(mode)
    }

    fun getLanguage(context: Context): String = prefs(context).getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM

    fun setLanguage(context: Context, languageTag: String) {
        prefs(context).edit().putString(KEY_LANGUAGE, languageTag).apply()
        applyLanguage(languageTag)
    }

    private fun applyTheme(mode: String) {
        AppCompatDelegate.setDefaultNightMode(
            when (mode) {
                THEME_LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                THEME_DARK -> AppCompatDelegate.MODE_NIGHT_YES
                else -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            }
        )
    }

    private fun applyLanguage(languageTag: String) {
        val locales = if (languageTag == LANGUAGE_SYSTEM) {
            LocaleListCompat.getEmptyLocaleList()
        } else {
            LocaleListCompat.forLanguageTags(languageTag)
        }
        AppCompatDelegate.setApplicationLocales(locales)
    }

    private fun prefs(context: Context) = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
