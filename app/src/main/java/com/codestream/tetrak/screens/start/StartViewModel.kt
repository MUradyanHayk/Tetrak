package com.codestream.tetrak.screens.start

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.codestream.tetrak.db.NoteDatabase
import com.codestream.tetrak.db.repository.NoteRepositoryImpl
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants

class StartViewModel(val app: Application) : AndroidViewModel(app) {
    fun initDatabase() {
        val dao = NoteDatabase.getInstance(app).getNoteDao()
        AppConstants.REPOSITORY = NoteRepositoryImpl(dao)
    }

    fun getAllNotes(): LiveData<MutableList<NoteModel>> {
        return AppConstants.REPOSITORY.allNotes
    }
}