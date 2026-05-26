package com.codestream.tetrak.adapter

import android.graphics.drawable.GradientDrawable
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.codestream.tetrak.databinding.ItemNoteHistoryBinding
import com.codestream.tetrak.model.NoteHistoryModel
import java.util.Date

class NoteHistoryAdapter : ListAdapter<NoteHistoryModel, NoteHistoryAdapter.HistoryViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val binding = ItemNoteHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class HistoryViewHolder(private val binding: ItemNoteHistoryBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NoteHistoryModel) = with(binding) {
            historyTitle.text = item.title
            historyDescription.text = item.description.ifBlank { root.context.getString(com.codestream.tetrak.R.string.no_description) }
            historySummary.text = root.context.getString(com.codestream.tetrak.R.string.changed_fields_value, item.changeSummary)
            historyDate.text = DateFormat.format("MMM d, yyyy  HH:mm", Date(item.changedAt))
            historyColor.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(item.color)
            }
            root.alpha = 0f
            root.translationY = 18f
            root.animate().alpha(1f).translationY(0f).setDuration(220L).start()
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<NoteHistoryModel>() {
        override fun areItemsTheSame(oldItem: NoteHistoryModel, newItem: NoteHistoryModel): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: NoteHistoryModel, newItem: NoteHistoryModel): Boolean = oldItem == newItem
    }
}
