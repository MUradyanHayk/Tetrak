package com.codestream.tetrak.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentThemeSettingsBinding
import com.codestream.tetrak.utils.AppSettings

class ThemeSettingsFragment : Fragment() {
    private var _binding: FragmentThemeSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentThemeSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        bindTheme()
        setupPicker()
        animateIntro()
    }

    private fun bindTheme() {
        binding.themeGroup.check(
            when (viewModel.theme()) {
                AppSettings.THEME_LIGHT -> R.id.theme_light
                AppSettings.THEME_DARK -> R.id.theme_dark
                else -> R.id.theme_system
            }
        )
    }

    private fun setupPicker() {
        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.theme_light -> AppSettings.THEME_LIGHT
                R.id.theme_dark -> AppSettings.THEME_DARK
                else -> AppSettings.THEME_SYSTEM
            }
            viewModel.updateTheme(mode)
        }
    }

    private fun animateIntro() = with(binding.optionsCard) {
        alpha = 0f
        translationY = 28f
        animate().alpha(1f).translationY(0f).setDuration(260L).start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
