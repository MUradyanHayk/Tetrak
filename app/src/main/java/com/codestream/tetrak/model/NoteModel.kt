package com.codestream.tetrak.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "note_table")
data class NoteModel(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo
    var title: String = "",

    @ColumnInfo
    var description: String = "",

    @ColumnInfo(defaultValue = "-10785537")
    var color: Int = DEFAULT_NOTE_COLOR,

    @ColumnInfo(defaultValue = "0")
    var createdAt: Long = 0L,

    @ColumnInfo(defaultValue = "0")
    var updatedAt: Long = 0L,

    @ColumnInfo(defaultValue = "0")
    var edited: Boolean = false
) : Serializable {
    companion object {
        const val DEFAULT_NOTE_COLOR: Int = -10785537
    }
}
