package com.codestream.tetrak.screens.detail

import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.databinding.FragmentDetailBinding
import com.codestream.tetrak.model.NoteModel

class DetailFragment : Fragment() {
    private var _binding: FragmentDetailBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DetailViewModel by viewModels()
    private var currentNote: NoteModel? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailBinding.inflate(inflater, container, false)
        currentNote = readNoteArgument()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        bindNote()
        animateIntro()
        setupClicks()
    }

    private fun readNoteArgument(): NoteModel? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("note", NoteModel::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getSerializable("note") as? NoteModel
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun bindNote() = with(binding) {
        val noteColor = currentNote?.color ?: NoteModel.DEFAULT_NOTE_COLOR
        title.text = currentNote?.title.orEmpty()
        description.text = currentNote?.description?.ifBlank { getString(com.codestream.tetrak.R.string.no_description) }.orEmpty()
        detailCard.strokeColor = noteColor
        noteIcon.setColorFilter(noteColor)
        noteAccent.background = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 20f
            setColor(noteColor)
        }
    }

    private fun setupClicks() {
        binding.deleteBtn.setOnClickListener {
            currentNote?.let { note ->
                binding.deleteBtn.isEnabled = false
                viewModel.delete(note) { findNavController().navigateUp() }
            }
        }
    }

    private fun animateIntro() = with(binding.detailCard) {
        scaleX = 0.96f
        scaleY = 0.96f
        alpha = 0f
        animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(360L).start()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
