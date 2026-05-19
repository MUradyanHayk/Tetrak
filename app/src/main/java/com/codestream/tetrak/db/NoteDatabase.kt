package com.codestream.tetrak.db

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.codestream.tetrak.db.dao.NoteDao
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants

@Database(entities = [NoteModel::class], version = AppConstants.DATABASE_VERSION)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao

    companion object {
        private var database: NoteDatabase? = null

        @Synchronized
        fun getInstance(context: Context): NoteDatabase {
            database = database ?: Room.databaseBuilder(context, NoteDatabase::class.java, "note_db").build()
            return database!!
        }
    }
}