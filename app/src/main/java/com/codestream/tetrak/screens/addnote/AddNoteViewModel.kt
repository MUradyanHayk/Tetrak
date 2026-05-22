package com.codestream.tetrak.screens.addnote

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddNoteViewModel : ViewModel() {
    fun insert(noteModel: NoteModel, onSuccess: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            AppConstants.REPOSITORY.insertNote(noteModel) {
                onSuccess()
            }
        }
}