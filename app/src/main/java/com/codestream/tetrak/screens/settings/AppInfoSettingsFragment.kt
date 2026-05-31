package com.codestream.tetrak.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.BuildConfig
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentAppInfoSettingsBinding

class AppInfoSettingsFragment : Fragment() {
    private var _binding: FragmentAppInfoSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAppInfoSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.appVersion.text = getString(R.string.app_version_value, BuildConfig.VERSION_NAME)
        animateIntro()
    }

    private fun animateIntro() = with(binding.contentCard) {
        alpha = 0f
        translationY = 28f
        animate().alpha(1f).translationY(0f).setDuration(260L).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
