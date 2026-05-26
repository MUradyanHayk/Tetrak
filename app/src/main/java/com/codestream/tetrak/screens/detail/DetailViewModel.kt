package com.codestream.tetrak.screens.detail

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.codestream.tetrak.ai.GeminiTitleGenerator
import com.codestream.tetrak.db.NoteDatabase
import com.codestream.tetrak.db.repository.NoteRepository
import com.codestream.tetrak.db.repository.NoteRepositoryImpl
import com.codestream.tetrak.model.NoteHistoryModel
import com.codestream.tetrak.model.NoteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: NoteRepository = NoteRepositoryImpl(
        NoteDatabase.getInstance(app).getNoteDao()
    )
    private val titleGenerator = GeminiTitleGenerator(app)

    fun update(previousNote: NoteModel, updatedNote: NoteModel, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.updateNote(previousNote, updatedNote)
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun delete(noteModel: NoteModel, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteNote(noteModel)
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }

    fun getHistory(noteId: Int): LiveData<List<NoteHistoryModel>> = repository.getHistory(noteId)
    fun generateTitle(description: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { titleGenerator.generateTitle(description) }
                .onSuccess { title -> withContext(Dispatchers.Main) { onSuccess(title) } }
                .onFailure { error -> withContext(Dispatchers.Main) { onError(error.message.orEmpty()) } }
        }
    }
}
