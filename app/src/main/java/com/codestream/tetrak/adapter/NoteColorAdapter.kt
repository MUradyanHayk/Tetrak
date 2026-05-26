package com.codestream.tetrak.adapter

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.ItemNoteColorBinding

class NoteColorAdapter(
    private val presetColors: List<Int>,
    private var selectedColor: Int,
    private val onColorSelected: (Int) -> Unit,
    private val onCustomColorClick: () -> Unit
) : RecyclerView.Adapter<NoteColorAdapter.ColorViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ColorViewHolder {
        val binding = ItemNoteColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ColorViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ColorViewHolder, position: Int) {
        val isCustomItem = position == presetColors.size
        val color = if (isCustomItem) selectedColor else presetColors[position]
        val isSelected = if (isCustomItem) !presetColors.contains(selectedColor) else color == selectedColor
        holder.bind(color = color, isSelected = isSelected, isCustomItem = isCustomItem)
    }

    override fun getItemCount(): Int = presetColors.size + 1

    fun select(color: Int) {
        val oldSelectedColor = selectedColor
        selectedColor = color
        val oldIndex = presetColors.indexOf(oldSelectedColor).takeIf { it != -1 } ?: presetColors.size
        val newIndex = presetColors.indexOf(color).takeIf { it != -1 } ?: presetColors.size
        notifyItemChanged(oldIndex)
        if (oldIndex != newIndex) notifyItemChanged(newIndex)
    }

    inner class ColorViewHolder(private val binding: ItemNoteColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(color: Int, isSelected: Boolean, isCustomItem: Boolean) = with(binding) {
            val context = root.context
            val strokeColor = if (isSelected) color else ContextCompat.getColor(context, R.color.surface_variant)
            colorSwatch.background = if (isCustomItem && !isSelected) {
                GradientDrawable(
                    GradientDrawable.Orientation.TL_BR,
                    intArrayOf(
                        Color.parseColor("#FF6B8A"),
                        Color.parseColor("#FFB84D"),
                        Color.parseColor("#24C6A1"),
                        Color.parseColor("#5B6CFF"),
                        Color.parseColor("#A162F7")
                    )
                ).apply {
                    shape = GradientDrawable.OVAL
                }
            } else {
                GradientDrawable().apply {
                    shape = GradientDrawable.OVAL
                    setColor(color)
                }
            }

            root.strokeColor = strokeColor
            root.strokeWidth = if (isSelected) context.resources.getDimensionPixelSize(R.dimen.color_picker_selected_stroke) else 1
            customIcon.visibility = if (isCustomItem) View.VISIBLE else View.GONE
            customIcon.alpha = if (isCustomItem && !isSelected) 1f else 0f
            checkIcon.alpha = if (isSelected) 1f else 0f
            checkIcon.setColorFilter(if (isColorDark(color)) Color.WHITE else Color.BLACK)
            root.animate()
                .scaleX(if (isSelected) 1.07f else 1f)
                .scaleY(if (isSelected) 1.07f else 1f)
                .rotation(if (isSelected) 2f else 0f)
                .setDuration(180L)
                .start()

            root.setOnClickListener {
                if (isCustomItem) {
                    root.animate().scaleX(0.94f).scaleY(0.94f).setDuration(70L).withEndAction {
                        root.animate().scaleX(if (isSelected) 1.07f else 1f).scaleY(if (isSelected) 1.07f else 1f).setDuration(110L).start()
                    }.start()
                    onCustomColorClick()
                } else if (selectedColor != color) {
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
