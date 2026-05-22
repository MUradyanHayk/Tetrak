package com.codestream.tetrak.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.codestream.tetrak.databinding.ItemLayoutBinding
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants
import java.lang.ref.WeakReference

interface NoteAdapterDelegate {
    fun onClick(note: NoteModel)
}

class NoteAdapter(val delegate: NoteAdapterDelegate) : RecyclerView.Adapter<NoteAdapter.NoteViewHolder>() {
    var listNote = mutableListOf<NoteModel>()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val binding = ItemLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        val note = listNote[position]
        holder.bind(note)
        holder.itemView.setOnClickListener {
            Toast.makeText(AppConstants.mainApplication, "$position", Toast.LENGTH_SHORT).show()
            delegate.onClick(note)
        }
    }

    override fun getItemCount(): Int {
        return listNote.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun setList(list: MutableList<NoteModel>) {
        listNote = list
        notifyDataSetChanged()
    }

    class NoteViewHolder(val binding: ItemLayoutBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: NoteModel) {
            binding.itemTitle.text = note.title
        }
    }
}
