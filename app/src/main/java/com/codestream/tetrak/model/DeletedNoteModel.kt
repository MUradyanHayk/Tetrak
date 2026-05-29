package com.codestream.tetrak.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_note_table")
data class DeletedNoteModel(
    @PrimaryKey
    var originalId: Int = 0,

    @ColumnInfo
    var title: String = "",

    @ColumnInfo
    var description: String = "",

    @ColumnInfo(defaultValue = "-10785537")
    var color: Int = NoteModel.DEFAULT_NOTE_COLOR,

    @ColumnInfo(defaultValue = "0")
    var createdAt: Long = 0L,

    @ColumnInfo(defaultValue = "0")
    var updatedAt: Long = 0L,

    @ColumnInfo(defaultValue = "0")
    var edited: Boolean = false,

    @ColumnInfo(defaultValue = "0")
    var deletedAt: Long = 0L,

    @ColumnInfo(defaultValue = "0")
    var expiresAt: Long = 0L
)
