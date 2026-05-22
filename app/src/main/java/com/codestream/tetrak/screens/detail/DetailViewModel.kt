package com.codestream.tetrak.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DetailViewModel : ViewModel() {
    fun delete(noteModel: NoteModel, onSuccess: () -> Unit) =
        viewModelScope.launch(Dispatchers.IO) {
            AppConstants.REPOSITORY.deleteNote(noteModel) {
                onSuccess()
            }
        }
}