package com.codestream.tetrak.screens.addnote

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.adapter.NoteColorAdapter
import com.codestream.tetrak.databinding.FragmentAddNoteBinding
import com.codestream.tetrak.model.NoteModel

class AddNoteFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddNoteViewModel by viewModels()
    private val colorOptions = listOf(
        Color.parseColor("#5B6CFF"),
        Color.parseColor("#FFB84D"),
        Color.parseColor("#24C6A1"),
        Color.parseColor("#FF6B8A"),
        Color.parseColor("#A162F7"),
        Color.parseColor("#4DB6FF"),
        Color.parseColor("#FF8A3D")
    )
    private var selectedColor: Int = NoteModel.DEFAULT_NOTE_COLOR

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupColorPicker()
        animateIntro()
        setupValidation()
        setupClicks()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupColorPicker() {
        binding.colorPicker.adapter = NoteColorAdapter(colorOptions, selectedColor) { color ->
            selectedColor = color
            binding.editorCard.strokeColor = color
        }
    }

    private fun setupValidation() {
        binding.edAddTitle.doAfterTextChanged { binding.titleInput.error = null }
    }

    private fun setupClicks() {
        binding.addNoteBtn.setOnClickListener {
            val title = binding.edAddTitle.text?.toString().orEmpty().trim()
            val description = binding.edAddDesc.text?.toString().orEmpty().trim()

            if (title.isBlank()) {
                binding.titleInput.error = getString(R.string.title_required)
                return@setOnClickListener
            }

            binding.addNoteBtn.isEnabled = false
            viewModel.insert(NoteModel(title = title, description = description, color = selectedColor)) {
                findNavController().navigateUp()
            }
        }
    }

    private fun animateIntro() = with(binding.editorCard) {
        translationY = 48f
        alpha = 0f
        animate().translationY(0f).alpha(1f).setDuration(450L).start()
    }

    override fun onDestroyView() {
        binding.colorPicker.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
