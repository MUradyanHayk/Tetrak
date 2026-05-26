package com.codestream.tetrak.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codestream.tetrak.db.dao.NoteDao
import com.codestream.tetrak.model.NoteHistoryModel
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants

@Database(entities = [NoteModel::class, NoteHistoryModel::class], version = AppConstants.DATABASE_VERSION, exportSchema = false)
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

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE note_table ADD COLUMN createdAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE note_table ADD COLUMN updatedAt INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE note_table ADD COLUMN edited INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE note_table SET createdAt = CASE WHEN createdAt = 0 THEN strftime('%s','now') * 1000 ELSE createdAt END")
                db.execSQL("UPDATE note_table SET updatedAt = CASE WHEN updatedAt = 0 THEN createdAt ELSE updatedAt END")
                db.execSQL("CREATE TABLE IF NOT EXISTS note_history_table (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `noteId` INTEGER NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `color` INTEGER NOT NULL, `changedAt` INTEGER NOT NULL, `changeSummary` TEXT NOT NULL, FOREIGN KEY(`noteId`) REFERENCES `note_table`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_note_history_table_noteId ON note_history_table (`noteId`)")
            }
        }

        fun getInstance(context: Context): NoteDatabase {
            return database ?: synchronized(this) {
                database ?: Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "note_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                    .also { database = it }
            }
        }
    }
}
