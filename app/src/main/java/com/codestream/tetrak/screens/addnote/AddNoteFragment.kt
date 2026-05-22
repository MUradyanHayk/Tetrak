package com.codestream.tetrak.screens.addnote

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentAddNoteBinding
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants

class AddNoteFragment : Fragment() {
    private lateinit var binding: FragmentAddNoteBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentAddNoteBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val viewModel = ViewModelProvider(this)[AddNoteViewModel::class.java]
        binding.addNoteBtn.setOnClickListener {
            val title = binding.edAddTitle.text.toString()
            val description = binding.edAddDesc.text.toString()
            viewModel.insert(NoteModel(title = title, description = description)) {}
            AppConstants.mainApplication.navController.navigate(R.id.action_addNoteFragment_to_startFragment)
        }
        binding.backBtn.setOnClickListener {
            AppConstants.mainApplication.navController.navigate(R.id.action_addNoteFragment_to_startFragment)
        }
    }

    companion object {
        const val TAG = "AddNoteFragment"
    }
}