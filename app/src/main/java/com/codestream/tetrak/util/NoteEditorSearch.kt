package com.codestream.tetrak.util

import android.widget.EditText

class NoteEditorSearch(private val fields: List<EditText>) {
    fun findNext(query: String): Boolean {
        val needle = query.trim()
        if (needle.isBlank()) return false

        val activeIndex = fields.indexOfFirst { it.hasFocus() }.let { if (it >= 0) it else 0 }
        val activeField = fields.getOrNull(activeIndex)
        val activeStart = activeField?.selectionEnd?.coerceAtLeast(0) ?: 0

        for (round in 0..1) {
            for (offset in fields.indices) {
                val index = (activeIndex + offset) % fields.size
                val field = fields[index]
                val start = when {
                    round == 0 && index == activeIndex -> activeStart
                    round == 0 -> 0
                    else -> 0
                }
                val found = field.text?.toString().orEmpty().indexOf(needle, start, ignoreCase = true)
                if (found >= 0) {
                    field.requestFocus()
                    field.setSelection(found, found + needle.length)
                    return true
                }
            }
        }
        return false
    }
}
