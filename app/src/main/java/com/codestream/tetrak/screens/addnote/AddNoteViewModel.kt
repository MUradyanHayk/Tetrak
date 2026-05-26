package com.codestream.tetrak.screens.addnote

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codestream.tetrak.ai.GeminiTitleGenerator
import com.codestream.tetrak.db.NoteDatabase
import com.codestream.tetrak.db.repository.NoteRepository
import com.codestream.tetrak.db.repository.NoteRepositoryImpl
import com.codestream.tetrak.model.NoteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AddNoteViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: NoteRepository = NoteRepositoryImpl(
        NoteDatabase.getInstance(app).getNoteDao()
    )
    private val titleGenerator = GeminiTitleGenerator(app)

    fun insert(noteModel: NoteModel, onSuccess: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertNote(noteModel)
            withContext(Dispatchers.Main) { onSuccess() }
        }
    }
    fun generateTitle(description: String, onSuccess: (String) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            runCatching { titleGenerator.generateTitle(description) }
                .onSuccess { title -> withContext(Dispatchers.Main) { onSuccess(title) } }
                .onFailure { error -> withContext(Dispatchers.Main) { onError(error.message.orEmpty()) } }
        }
    }
}
