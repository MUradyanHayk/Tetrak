package com.codestream.tetrak.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.codestream.tetrak.db.dao.NoteDao
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants

@Database(entities = [NoteModel::class], version = AppConstants.DATABASE_VERSION, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao

    companion object {
        @Volatile
        private var database: NoteDatabase? = null

        fun getInstance(context: Context): NoteDatabase {
            return database ?: synchronized(this) {
                database ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_db"
                ).build().also { database = it }
            }
        }
    }
}
