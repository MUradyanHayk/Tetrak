package com.codestream.tetrak.util

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import kotlin.math.max
import kotlin.math.min

class NoteEditorHistory(
    private val fields: List<EditText>,
    private val onStateChanged: () -> Unit
) {
    private data class Snapshot(
        val values: List<String>,
        val focusedIndex: Int,
        val selectionStart: Int,
        val selectionEnd: Int
    )

    private val undoStack = mutableListOf<Snapshot>()
    private val redoStack = mutableListOf<Snapshot>()
    private var isRestoring = false
    private var isAttached = false

    fun attach() {
        if (isAttached) return
        isAttached = true
        undoStack.clear()
        redoStack.clear()
        undoStack += currentSnapshot()
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) = Unit
            override fun afterTextChanged(s: Editable?) {
                if (isRestoring) return
                val snapshot = currentSnapshot()
                if (undoStack.lastOrNull()?.values != snapshot.values) {
                    undoStack += snapshot
                    redoStack.clear()
                    onStateChanged()
                }
            }
        }
        fields.forEach { it.addTextChangedListener(watcher) }
        onStateChanged()
    }

    fun reset() {
        undoStack.clear()
        redoStack.clear()
        undoStack += currentSnapshot()
        onStateChanged()
    }

    fun canUndo(): Boolean = undoStack.size > 1

    fun canRedo(): Boolean = redoStack.isNotEmpty()

    fun undo() {
        if (!canUndo()) return
        redoStack += undoStack.removeAt(undoStack.lastIndex)
        restore(undoStack.last())
        onStateChanged()
    }

    fun redo() {
        val snapshot = redoStack.removeLastOrNull() ?: return
        undoStack += snapshot
        restore(snapshot)
        onStateChanged()
    }

    private fun currentSnapshot(): Snapshot {
        val focusedIndex = fields.indexOfFirst { it.hasFocus() }.let { if (it >= 0) it else 0 }
        val focused = fields.getOrNull(focusedIndex)
        return Snapshot(
            values = fields.map { it.text?.toString().orEmpty() },
            focusedIndex = focusedIndex,
            selectionStart = focused?.selectionStart ?: 0,
            selectionEnd = focused?.selectionEnd ?: 0
        )
    }

    private fun restore(snapshot: Snapshot) {
        isRestoring = true
        fields.forEachIndexed { index, field ->
            val value = snapshot.values.getOrNull(index).orEmpty()
            if (field.text?.toString().orEmpty() != value) {
                field.setText(value)
            }
        }
        val focused = fields.getOrNull(snapshot.focusedIndex)
        if (focused != null) {
            focused.requestFocus()
            val length = focused.text?.length ?: 0
            focused.setSelection(
                min(max(snapshot.selectionStart, 0), length),
                min(max(snapshot.selectionEnd, 0), length)
            )
        }
        isRestoring = false
    }
}
