package com.codestream.tetrak.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.codestream.tetrak.model.NoteHistoryModel
import com.codestream.tetrak.model.NoteModel

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(noteModel: NoteModel): Long

    @Update
    suspend fun update(noteModel: NoteModel)

    @Delete
    suspend fun delete(noteModel: NoteModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(noteHistoryModel: NoteHistoryModel)

    @Query("SELECT * FROM note_history_table WHERE noteId = :noteId ORDER BY changedAt DESC")
    fun getHistory(noteId: Int): LiveData<List<NoteHistoryModel>>

    @Query("SELECT * FROM note_table ORDER BY updatedAt DESC, id DESC")
    fun getAllNotes(): LiveData<List<NoteModel>>
}
