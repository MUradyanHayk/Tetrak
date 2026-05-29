package com.codestream.tetrak.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.ItemDeletedNoteBinding
import com.codestream.tetrak.model.DeletedNoteModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DeletedNoteAdapter(
    private val onRestoreClick: (DeletedNoteModel) -> Unit,
    private val onDeleteForeverClick: (DeletedNoteModel) -> Unit
) : ListAdapter<DeletedNoteModel, DeletedNoteAdapter.DeletedNoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeletedNoteViewHolder {
        val binding = ItemDeletedNoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeletedNoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeletedNoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class DeletedNoteViewHolder(
        private val binding: ItemDeletedNoteBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        private val dateFormat = SimpleDateFormat("MMM d, yyyy - HH:mm", Locale.getDefault())

        fun bind(note: DeletedNoteModel) = with(binding) {
            title.text = note.title.ifBlank { root.context.getString(R.string.untitled_note) }
            description.text = note.description.ifBlank { root.context.getString(R.string.no_description) }
            deletedDate.text = root.context.getString(
                R.string.deleted_on_value,
                dateFormat.format(Date(note.deletedAt))
            )
            expiresDate.text = root.context.getString(
                R.string.deleted_note_expires_value,
                dateFormat.format(Date(note.expiresAt))
            )
            colorStripe.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 24f
                setColor(note.color)
            }
            restoreButton.setOnClickListener {
                animateActionTap(restoreButton)
                onRestoreClick(note)
            }
            deleteForeverButton.setOnClickListener {
                animateActionTap(deleteForeverButton)
                onDeleteForeverClick(note)
            }
            root.alpha = 0f
            root.translationY = 18f
            root.animate().alpha(1f).translationY(0f).setDuration(220L).start()
        }

        private fun animateActionTap(view: android.view.View) {
            view.animate().scaleX(0.9f).scaleY(0.9f).setDuration(70L).withEndAction {
                view.animate().scaleX(1f).scaleY(1f).setDuration(110L).start()
            }.start()
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<DeletedNoteModel>() {
        override fun areItemsTheSame(oldItem: DeletedNoteModel, newItem: DeletedNoteModel): Boolean {
            return oldItem.originalId == newItem.originalId
        }

        override fun areContentsTheSame(oldItem: DeletedNoteModel, newItem: DeletedNoteModel): Boolean {
            return oldItem == newItem
        }
    }
}
