package com.codestream.tetrak.db.repository

import androidx.lifecycle.LiveData
import com.codestream.tetrak.db.dao.NoteDao
import com.codestream.tetrak.model.DeletedNoteModel
import com.codestream.tetrak.model.NoteHistoryModel
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NoteRepositoryImpl(private val noteDao: NoteDao) : NoteRepository {
    override val allNotes: LiveData<List<NoteModel>> = noteDao.getAllNotes()

    override suspend fun insertNote(noteModel: NoteModel) {
        val now = System.currentTimeMillis()
        noteDao.insert(
            noteModel.copy(
                title = noteModel.title.ifBlank { defaultTitle(now) },
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
                title = updatedNote.title.ifBlank { defaultTitle(now) },
                createdAt = previousNote.createdAt.takeIf { it > 0L } ?: now,
                updatedAt = now,
                edited = true
            )
        )
    }

    override suspend fun deleteNote(noteModel: NoteModel) {
        val now = System.currentTimeMillis()
        val retentionMillis = AppConstants.DELETED_NOTES_RETENTION_DAYS * 24L * 60L * 60L * 1000L
        noteDao.insertDeletedNote(
            DeletedNoteModel(
                originalId = noteModel.id,
                title = noteModel.title.ifBlank { defaultTitle(noteModel.createdAt.takeIf { it > 0L } ?: now) },
                description = noteModel.description,
                color = noteModel.color,
                createdAt = noteModel.createdAt,
                updatedAt = noteModel.updatedAt,
                edited = noteModel.edited,
                deletedAt = now,
                expiresAt = now + retentionMillis
            )
        )
        noteDao.delete(noteModel)
        cleanupExpiredDeletedNotes()
    }

    override suspend fun restoreDeletedNote(deletedNoteModel: DeletedNoteModel) {
        val now = System.currentTimeMillis()
        noteDao.insert(
            NoteModel(
                id = deletedNoteModel.originalId,
                title = deletedNoteModel.title.ifBlank { defaultTitle(now) },
                description = deletedNoteModel.description,
                color = deletedNoteModel.color,
                createdAt = deletedNoteModel.createdAt.takeIf { it > 0L } ?: now,
                updatedAt = now,
                edited = deletedNoteModel.edited
            )
        )
        noteDao.deleteDeletedNote(deletedNoteModel)
    }

    override suspend fun permanentlyDeleteDeletedNote(deletedNoteModel: DeletedNoteModel) {
        noteDao.deleteDeletedNote(deletedNoteModel)
    }

    override suspend fun cleanupExpiredDeletedNotes() {
        noteDao.deleteExpiredDeletedNotes(System.currentTimeMillis())
    }

    override fun getHistory(noteId: Int): LiveData<List<NoteHistoryModel>> = noteDao.getHistory(noteId)

    override fun getDeletedNotes(): LiveData<List<DeletedNoteModel>> = noteDao.getDeletedNotes()

    private fun defaultTitle(timestamp: Long): String {
        val formatter = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())
        return "Note ${formatter.format(Date(timestamp))}"
    }

    private fun buildChangeSummary(previousNote: NoteModel, updatedNote: NoteModel): String {
        val changes = buildList {
            if (previousNote.title != updatedNote.title) add("Title")
            if (previousNote.description != updatedNote.description) add("Description")
            if (previousNote.color != updatedNote.color) add("Color")
        }
        return if (changes.isEmpty()) "No visible changes" else changes.joinToString(separator = ", ")
    }
}
