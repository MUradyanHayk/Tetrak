package com.codestream.tetrak.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codestream.tetrak.db.dao.NoteDao
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants

@Database(entities = [NoteModel::class], version = AppConstants.DATABASE_VERSION, exportSchema = false)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun getNoteDao(): NoteDao

    companion object {
        @Volatile
        private var database: NoteDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE note_table ADD COLUMN color INTEGER NOT NULL DEFAULT ${NoteModel.DEFAULT_NOTE_COLOR}")
            }
        }

        fun getInstance(context: Context): NoteDatabase {
            return database ?: synchronized(this) {
                database ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { database = it }
            }
        }
    }
}
