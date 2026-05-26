package com.codestream.tetrak.screens.addnote

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.databinding.FragmentAddNoteBinding
import com.codestream.tetrak.model.NoteModel

class AddNoteFragment : Fragment() {
    private var _binding: FragmentAddNoteBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AddNoteViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAddNoteBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        animateIntro()
        setupValidation()
        setupClicks()
    }

    private fun setupValidation() {
        binding.edAddTitle.doAfterTextChanged { binding.titleInput.error = null }
    }

    private fun setupClicks() {
        binding.addNoteBtn.setOnClickListener {
            val title = binding.edAddTitle.text?.toString().orEmpty().trim()
            val description = binding.edAddDesc.text?.toString().orEmpty().trim()

            if (title.isBlank()) {
                binding.titleInput.error = "Title is required"
                return@setOnClickListener
            }

            binding.addNoteBtn.isEnabled = false
            viewModel.insert(NoteModel(title = title, description = description)) {
                findNavController().navigateUp()
            }
        }

        binding.backBtn.setOnClickListener { findNavController().navigateUp() }
    }

    private fun animateIntro() = with(binding.editorCard) {
        translationY = 48f
        alpha = 0f
        animate().translationY(0f).alpha(1f).setDuration(450L).start()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
