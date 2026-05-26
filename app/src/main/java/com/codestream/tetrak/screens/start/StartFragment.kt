package com.codestream.tetrak.screens.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import com.codestream.tetrak.R
import com.codestream.tetrak.adapter.NoteAdapter
import com.codestream.tetrak.adapter.NoteAdapterDelegate
import com.codestream.tetrak.databinding.FragmentStartBinding
import com.codestream.tetrak.model.NoteModel

class StartFragment : Fragment(), NoteAdapterDelegate {
    private var _binding: FragmentStartBinding? = null
    private val binding get() = _binding!!
    private val viewModel: StartViewModel by viewModels()
    private val adapter by lazy { NoteAdapter(this) }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupList()
        setupClicks()
        animateIntro()
        observeNotes()
    }

    private fun setupList() = with(binding.rvNotes) {
        adapter = this@StartFragment.adapter
        itemAnimator = DefaultItemAnimator()
        setHasFixedSize(true)
    }

    private fun setupClicks() {
        binding.nextButton.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_addNoteFragment)
        }
        binding.settingsButton.setOnClickListener {
            findNavController().navigate(R.id.action_startFragment_to_settingsFragment)
        }
    }

    private fun observeNotes() {
        viewModel.getAllNotes().observe(viewLifecycleOwner) { notes ->
            adapter.submitList(notes)
            val isEmpty = notes.isEmpty()
            binding.emptyStateGroup.isVisible = isEmpty
            binding.rvNotes.isVisible = !isEmpty
            if (!isEmpty) binding.rvNotes.scheduleLayoutAnimation()
        }
    }

    private fun animateIntro() = with(binding) {
        headerContainer.translationY = -32f
        headerContainer.alpha = 0f
        headerContainer.animate().translationY(0f).alpha(1f).setDuration(450L).start()
        nextButton.scaleX = 0f
        nextButton.scaleY = 0f
        nextButton.animate().scaleX(1f).scaleY(1f).setStartDelay(180L).setDuration(300L).start()
    }

    override fun onClick(note: NoteModel) {
        findNavController().navigate(
            R.id.action_startFragment_to_detailFragment,
            bundleOf("note" to note)
        )
    }

    override fun onDestroyView() {
        binding.rvNotes.adapter = null
        _binding = null
        super.onDestroyView()
    }
}
