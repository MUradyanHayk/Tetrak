package com.codestream.tetrak.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentSettingsBinding
import com.codestream.tetrak.utils.AppSettings

class SettingsFragment : Fragment() {
    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        bindCurrentSettings()
        setupThemePicker()
        setupLanguagePicker()
        animateIntro()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun bindCurrentSettings() = with(binding) {
        themeGroup.check(
            when (viewModel.theme()) {
                AppSettings.THEME_LIGHT -> R.id.theme_light
                AppSettings.THEME_DARK -> R.id.theme_dark
                else -> R.id.theme_system
            }
        )
        languageGroup.check(
            when (viewModel.language()) {
                AppSettings.LANGUAGE_ENGLISH -> R.id.language_english
                AppSettings.LANGUAGE_ARMENIAN -> R.id.language_armenian
                AppSettings.LANGUAGE_RUSSIAN -> R.id.language_russian
                AppSettings.LANGUAGE_ARABIC -> R.id.language_arabic
                AppSettings.LANGUAGE_PERSIAN -> R.id.language_persian
                else -> R.id.language_system
            }
        )
    }

    private fun setupThemePicker() {
        binding.themeGroup.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.theme_light -> AppSettings.THEME_LIGHT
                R.id.theme_dark -> AppSettings.THEME_DARK
                else -> AppSettings.THEME_SYSTEM
            }
            viewModel.updateTheme(mode)
        }
    }

    private fun setupLanguagePicker() {
        binding.languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val language = when (checkedId) {
                R.id.language_english -> AppSettings.LANGUAGE_ENGLISH
                R.id.language_armenian -> AppSettings.LANGUAGE_ARMENIAN
                R.id.language_russian -> AppSettings.LANGUAGE_RUSSIAN
                R.id.language_arabic -> AppSettings.LANGUAGE_ARABIC
                R.id.language_persian -> AppSettings.LANGUAGE_PERSIAN
                else -> AppSettings.LANGUAGE_SYSTEM
            }
            viewModel.updateLanguage(language)
        }
    }

    private fun animateIntro() = with(binding) {
        themeCard.translationY = 32f
        languageCard.translationY = 32f
        themeCard.alpha = 0f
        languageCard.alpha = 0f
        themeCard.animate().translationY(0f).alpha(1f).setDuration(280L).start()
        languageCard.animate().translationY(0f).alpha(1f).setStartDelay(90L).setDuration(280L).start()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
