package com.codestream.tetrak.screens.deletednotes

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.codestream.tetrak.db.NoteDatabase
import com.codestream.tetrak.db.repository.NoteRepository
import com.codestream.tetrak.db.repository.NoteRepositoryImpl
import com.codestream.tetrak.model.DeletedNoteModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DeletedNotesViewModel(app: Application) : AndroidViewModel(app) {
    private val repository: NoteRepository = NoteRepositoryImpl(
        NoteDatabase.getInstance(app).getNoteDao()
    )

    val deletedNotes: LiveData<List<DeletedNoteModel>> = repository.getDeletedNotes()

    init {
        viewModelScope.launch(Dispatchers.IO) { repository.cleanupExpiredDeletedNotes() }
    }

    fun restore(note: DeletedNoteModel, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.restoreDeletedNote(note)
            withContext(Dispatchers.Main) { onDone() }
        }
    }

    fun deleteForever(note: DeletedNoteModel, onDone: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.permanentlyDeleteDeletedNote(note)
            withContext(Dispatchers.Main) { onDone() }
        }
    }
}
