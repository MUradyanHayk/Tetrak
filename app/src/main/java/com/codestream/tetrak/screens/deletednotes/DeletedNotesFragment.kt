package com.codestream.tetrak.screens.deletednotes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.codestream.tetrak.R
import com.codestream.tetrak.adapter.DeletedNoteAdapter
import com.codestream.tetrak.databinding.FragmentDeletedNotesBinding
import com.codestream.tetrak.model.DeletedNoteModel
import com.codestream.tetrak.utils.AppConstants
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar

class DeletedNotesFragment : Fragment() {
    private var _binding: FragmentDeletedNotesBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DeletedNotesViewModel by viewModels()
    private var adapter: DeletedNoteAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDeletedNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupList()
        observeDeletedNotes()
        animateIntro()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.retentionInfo.text = getString(
            R.string.deleted_notes_retention,
            AppConstants.DELETED_NOTES_RETENTION_DAYS
        )
    }

    private fun setupList() {
        adapter = DeletedNoteAdapter(
            onRestoreClick = { note -> restoreNote(note) },
            onDeleteForeverClick = { note -> confirmDeleteForever(note) }
        )
        binding.deletedNotesList.layoutManager = LinearLayoutManager(requireContext())
        binding.deletedNotesList.adapter = adapter
    }

    private fun observeDeletedNotes() {
        viewModel.deletedNotes.observe(viewLifecycleOwner) { notes ->
            adapter?.submitList(notes)
            binding.deletedNotesCount.text = resources.getQuantityString(
                R.plurals.deleted_notes_count,
                notes.size,
                notes.size
            )
            val isEmpty = notes.isEmpty()
            binding.emptyState.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.deletedNotesList.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }

    private fun restoreNote(note: DeletedNoteModel) {
        viewModel.restore(note) {
            Snackbar.make(binding.root, R.string.deleted_note_restored, Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun confirmDeleteForever(note: DeletedNoteModel) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_forever_title)
            .setMessage(R.string.delete_forever_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete_forever) { _, _ ->
                viewModel.deleteForever(note) {
                    Snackbar.make(binding.root, R.string.deleted_note_removed, Snackbar.LENGTH_SHORT).show()
                }
            }
            .show()
    }

    private fun animateIntro() = with(binding) {
        headerCard.alpha = 0f
        headerCard.translationY = 28f
        headerCard.animate().alpha(1f).translationY(0f).setDuration(260L).start()
        deletedNotesList.alpha = 0f
        deletedNotesList.translationY = 32f
        deletedNotesList.animate().alpha(1f).translationY(0f).setStartDelay(80L).setDuration(280L).start()
    }

    override fun onDestroyView() {
        binding.deletedNotesList.adapter = null
        adapter = null
        _binding = null
        super.onDestroyView()
    }
}
