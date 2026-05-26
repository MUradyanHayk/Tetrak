package com.codestream.tetrak.db.repository

import androidx.lifecycle.LiveData
import com.codestream.tetrak.db.dao.NoteDao
import com.codestream.tetrak.model.NoteHistoryModel
import com.codestream.tetrak.model.NoteModel

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {
    override val allNotes: LiveData<List<NoteModel>> = noteDao.getAllNotes()

    override suspend fun insertNote(noteModel: NoteModel) {
        val now = System.currentTimeMillis()
        noteDao.insert(
            noteModel.copy(
                createdAt = now,
                updatedAt = now,
                edited = false
            )
        )
    }

    override suspend fun updateNote(previousNote: NoteModel, updatedNote: NoteModel) {
        val now = System.currentTimeMillis()
        val summary = buildChangeSummary(previousNote, updatedNote)
        noteDao.insertHistory(
            NoteHistoryModel(
                noteId = previousNote.id,
                title = previousNote.title,
                description = previousNote.description,
                color = previousNote.color,
                changedAt = now,
                changeSummary = summary
            )
        )
        noteDao.update(
            updatedNote.copy(
                createdAt = previousNote.createdAt.takeIf { it > 0L } ?: now,
                updatedAt = now,
                edited = true
            )
        )
    }

    override suspend fun deleteNote(noteModel: NoteModel) {
        noteDao.delete(noteModel)
    }

    override fun getHistory(noteId: Int): LiveData<List<NoteHistoryModel>> = noteDao.getHistory(noteId)

    private fun buildChangeSummary(previousNote: NoteModel, updatedNote: NoteModel): String {
        val changes = buildList {
            if (previousNote.title != updatedNote.title) add("Title")
            if (previousNote.description != updatedNote.description) add("Description")
            if (previousNote.color != updatedNote.color) add("Color")
        }
        return if (changes.isEmpty()) "No visible changes" else changes.joinToString(separator = ", ")
    }
}
