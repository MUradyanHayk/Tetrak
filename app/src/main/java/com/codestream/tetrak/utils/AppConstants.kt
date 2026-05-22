package com.codestream.tetrak.utils

import com.codestream.tetrak.MainActivity
import com.codestream.tetrak.db.repository.NoteRepository

object AppConstants {
    lateinit var mainApplication: MainActivity
    lateinit var REPOSITORY : NoteRepository
    const val DATABASE_VERSION = 1
}