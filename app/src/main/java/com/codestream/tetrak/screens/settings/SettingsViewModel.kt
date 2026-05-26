package com.codestream.tetrak.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.codestream.tetrak.premium.PremiumManager
import com.codestream.tetrak.utils.AppSettings

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    fun theme(): String = AppSettings.getTheme(getApplication())
    fun language(): String = AppSettings.getLanguage(getApplication())
    fun updateTheme(mode: String) = AppSettings.setTheme(getApplication(), mode)
    fun updateLanguage(languageTag: String) = AppSettings.setLanguage(getApplication(), languageTag)
    fun isPremium(): Boolean = PremiumManager.isPremium(getApplication())
    fun setPremiumForDevelopment(enabled: Boolean) = PremiumManager.setPremiumForDevelopment(getApplication(), enabled)
}
