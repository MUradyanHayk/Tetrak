package com.codestream.tetrak.screens.start

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.codestream.tetrak.R
import com.codestream.tetrak.adapter.NoteAdapter
import com.codestream.tetrak.databinding.ActivityMainBinding
import com.codestream.tetrak.databinding.FragmentStartBinding
import com.codestream.tetrak.utils.AppConstants

class StartFragment : Fragment() {
    private lateinit var binding: FragmentStartBinding
    private var adapter: NoteAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentStartBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    fun init() {
        val viewModel = ViewModelProvider(this)[StartViewModel::class.java]
        viewModel.initDatabase()
        adapter = NoteAdapter()
        binding.rvNotes.adapter = adapter
        viewModel.getAllNotes().observe(viewLifecycleOwner) { notes ->
            notes.reversed()
            adapter?.setList(notes)
        }

        binding.nextButton.setOnClickListener {
            AppConstants.mainApplication.navController.navigate(R.id.action_startFragment_to_addNoteFragment)
        }
    }

    companion object {
        const val TAG = "StartFragment"
    }
}