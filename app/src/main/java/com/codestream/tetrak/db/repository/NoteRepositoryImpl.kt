package com.codestream.tetrak.db.repository

import androidx.lifecycle.LiveData
import com.codestream.tetrak.db.dao.NoteDao
import com.codestream.tetrak.model.NoteModel

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {
    override val allNotes: LiveData<MutableList<NoteModel>>
        get() = noteDao.getAllNotes()

    override suspend fun insertNote(noteModel: NoteModel, onSuccess: () -> Unit) {
        noteDao.insert(noteModel)
    }

    override suspend fun deleteNote(noteModel: NoteModel, onSuccess: () -> Unit) {
        noteDao.delete(noteModel)
    }
}