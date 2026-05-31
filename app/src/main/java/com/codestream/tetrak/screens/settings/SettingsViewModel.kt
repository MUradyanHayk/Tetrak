package com.codestream.tetrak.screens.settings

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.codestream.tetrak.db.NoteDatabase
import com.codestream.tetrak.db.repository.NoteRepository
import com.codestream.tetrak.db.repository.NoteRepositoryImpl
import com.codestream.tetrak.model.DeletedNoteModel
import com.codestream.tetrak.premium.PremiumManager
import com.codestream.tetrak.utils.AppConstants
import com.codestream.tetrak.utils.AppSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: NoteRepository = NoteRepositoryImpl(
        NoteDatabase.getInstance(app).getNoteDao()
    )

    val deletedNotes: LiveData<List<DeletedNoteModel>> = repository.getDeletedNotes()

    init {
        viewModelScope.launch(Dispatchers.IO) { repository.cleanupExpiredDeletedNotes() }
    }

    fun theme(): String = AppSettings.getTheme(getApplication())
    fun language(): String = AppSettings.getLanguage(getApplication())
    fun updateTheme(mode: String) = AppSettings.setTheme(getApplication(), mode)
    fun updateLanguage(languageTag: String) = AppSettings.setLanguage(getApplication(), languageTag)

    fun themeLabel(context: Context): String = context.getString(
        when (theme()) {
            AppSettings.THEME_LIGHT -> com.codestream.tetrak.R.string.theme_light
            AppSettings.THEME_DARK -> com.codestream.tetrak.R.string.theme_dark
            else -> com.codestream.tetrak.R.string.theme_system
        }
    )

    fun languageLabel(context: Context): String = context.getString(
        when (language()) {
            AppSettings.LANGUAGE_ENGLISH -> com.codestream.tetrak.R.string.language_english
            AppSettings.LANGUAGE_ARMENIAN -> com.codestream.tetrak.R.string.language_armenian
            AppSettings.LANGUAGE_RUSSIAN -> com.codestream.tetrak.R.string.language_russian
            AppSettings.LANGUAGE_ARABIC -> com.codestream.tetrak.R.string.language_arabic
            AppSettings.LANGUAGE_PERSIAN -> com.codestream.tetrak.R.string.language_persian
            AppSettings.LANGUAGE_SPANISH -> com.codestream.tetrak.R.string.language_spanish
            AppSettings.LANGUAGE_FRENCH -> com.codestream.tetrak.R.string.language_french
            AppSettings.LANGUAGE_GERMAN -> com.codestream.tetrak.R.string.language_german
            AppSettings.LANGUAGE_PORTUGUESE -> com.codestream.tetrak.R.string.language_portuguese
            AppSettings.LANGUAGE_HINDI -> com.codestream.tetrak.R.string.language_hindi
            AppSettings.LANGUAGE_CHINESE -> com.codestream.tetrak.R.string.language_chinese
            AppSettings.LANGUAGE_JAPANESE -> com.codestream.tetrak.R.string.language_japanese
            AppSettings.LANGUAGE_KOREAN -> com.codestream.tetrak.R.string.language_korean
            AppSettings.LANGUAGE_TURKISH -> com.codestream.tetrak.R.string.language_turkish
            AppSettings.LANGUAGE_UKRAINIAN -> com.codestream.tetrak.R.string.language_ukrainian
            else -> com.codestream.tetrak.R.string.language_system
        }
    )
    fun isPremium(): Boolean = AppConstants.HAS_PREMIUM_FEATURES && PremiumManager.isPremium(getApplication())
    fun setPremiumForDevelopment(enabled: Boolean) {
        if (AppConstants.HAS_PREMIUM_FEATURES) {
            PremiumManager.setPremiumForDevelopment(getApplication(), enabled)
        }
    }

    fun restoreDeletedNote(note: DeletedNoteModel, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreDeletedNote(note)
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun permanentlyDeleteDeletedNote(note: DeletedNoteModel, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.permanentlyDeleteDeletedNote(note)
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }
}
