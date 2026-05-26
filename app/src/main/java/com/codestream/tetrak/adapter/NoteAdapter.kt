package com.codestream.tetrak.adapter

import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codestream.tetrak.databinding.ItemLayoutBinding
import com.codestream.tetrak.model.NoteModel

interface NoteAdapterDelegate {
    fun onClick(note: NoteModel)
}

class NoteAdapter(
    private val delegate: NoteAdapterDelegate
) : ListAdapter<NoteModel, NoteAdapter.NoteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding, delegate)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class NoteViewHolder(
        private val binding: ItemLayoutBinding,
        private val delegate: NoteAdapterDelegate
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: NoteModel) = with(binding) {
            itemTitle.text = note.title
            itemDescription.text = note.description.ifBlank { root.context.getString(com.codestream.tetrak.R.string.no_description) }
            colorStrip.background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10f
                setColor(note.color)
            }
            noteIcon.setColorFilter(note.color)
            root.setOnClickListener { delegate.onClick(note) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<NoteModel>() {
        override fun areItemsTheSame(oldItem: NoteModel, newItem: NoteModel): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NoteModel, newItem: NoteModel): Boolean = oldItem == newItem
    }
}
