package com.codestream.tetrak.screens.detail

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentAddNoteBinding
import com.codestream.tetrak.databinding.FragmentDetailBinding
import com.codestream.tetrak.model.NoteModel
import com.codestream.tetrak.utils.AppConstants

class DetailFragment : Fragment() {
    private lateinit var binding: FragmentDetailBinding
    private var currentNote: NoteModel? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailBinding.inflate(layoutInflater, container, false)
        currentNote = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arguments?.getSerializable("note", NoteModel::class.java)
        } else {
            arguments?.getSerializable("note") as NoteModel
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init()
    }

    private fun init() {
        val viewModel = ViewModelProvider(this)[DetailViewModel::class.java]
        binding.title.text = currentNote?.title
        binding.description.text = currentNote?.description

        binding.deleteBtn.setOnClickListener {
            currentNote?.let { viewModel.delete(it) {} }
            AppConstants.mainApplication.navController.navigate(R.id.action_detailFragment_to_startFragment)
        }
        binding.backBtn.setOnClickListener {
            AppConstants.mainApplication.navController.navigate(R.id.action_detailFragment_to_startFragment)
        }
    }

    companion object {
        const val TAG = "DetailFragment"
    }
}