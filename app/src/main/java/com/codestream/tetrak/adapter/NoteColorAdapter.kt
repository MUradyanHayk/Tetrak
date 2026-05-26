package com.codestream.tetrak.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.ItemNoteColorBinding

class NoteColorAdapter(
    private val colors: List<Int>,
    private var selectedColor: Int,
    private val onColorSelected: (Int) -> Unit
) : RecyclerView.Adapter<NoteColorAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemNoteColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        holder.bind(colors[position], colors[position] == selectedColor)
    }

    override fun getItemCount(): Int = colors.size

    fun select(color: Int) {
        val oldIndex = colors.indexOf(selectedColor)
        val newIndex = colors.indexOf(color)
        selectedColor = color
        if (oldIndex != -1) notifyItemChanged(oldIndex)
        if (newIndex != -1) notifyItemChanged(newIndex)
    }

    inner class ColorViewHolder(private val binding: ItemNoteColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(color: Int, isSelected: Boolean) = with(binding) {
            val context = root.context
            colorSwatch.background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(color)
            }
            root.strokeColor = if (isSelected) color else ContextCompat.getColor(context, R.color.surface_variant)
            root.strokeWidth = if (isSelected) context.resources.getDimensionPixelSize(R.dimen.color_picker_selected_stroke) else 1
            checkIcon.alpha = if (isSelected) 1f else 0f
            checkIcon.setColorFilter(if (isColorDark(color)) Color.WHITE else Color.BLACK)
            root.scaleX = if (isSelected) 1.12f else 1f
            root.scaleY = if (isSelected) 1.12f else 1f
            root.animate().scaleX(if (isSelected) 1.12f else 1f).scaleY(if (isSelected) 1.12f else 1f).rotation(if (isSelected) 4f else 0f).setDuration(180L).start()
            root.setOnClickListener {
                if (selectedColor != color) {
                    select(color)
                    onColorSelected(color)
                }
            }
        }
    }

    private fun isColorDark(color: Int): Boolean {
        val darkness = 1 - (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255
        return darkness >= 0.5
    }
}
