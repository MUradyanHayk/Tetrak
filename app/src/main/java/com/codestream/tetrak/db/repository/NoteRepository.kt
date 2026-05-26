package com.codestream.tetrak.db.repository

import androidx.lifecycle.LiveData
import com.codestream.tetrak.model.NoteHistoryModel
import com.codestream.tetrak.model.NoteModel

interface NoteRepository {
    val allNotes: LiveData<List<NoteModel>>
    suspend fun insertNote(noteModel: NoteModel)
    suspend fun updateNote(previousNote: NoteModel, updatedNote: NoteModel)
    suspend fun deleteNote(noteModel: NoteModel)
    fun getHistory(noteId: Int): LiveData<List<NoteHistoryModel>>
}
