package com.codestream.tetrak.screens.settings

import android.app.Application
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
