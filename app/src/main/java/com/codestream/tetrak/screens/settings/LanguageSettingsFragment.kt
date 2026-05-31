package com.codestream.tetrak.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentLanguageSettingsBinding
import com.codestream.tetrak.utils.AppSettings

class LanguageSettingsFragment : Fragment() {
    private var _binding: FragmentLanguageSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLanguageSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        bindLanguage()
        setupPicker()
        animateIntro()
    }

    private fun bindLanguage() {
        binding.languageGroup.check(
            when (viewModel.language()) {
                AppSettings.LANGUAGE_ENGLISH -> R.id.language_english
                AppSettings.LANGUAGE_ARMENIAN -> R.id.language_armenian
                AppSettings.LANGUAGE_RUSSIAN -> R.id.language_russian
                AppSettings.LANGUAGE_ARABIC -> R.id.language_arabic
                AppSettings.LANGUAGE_PERSIAN -> R.id.language_persian
                AppSettings.LANGUAGE_SPANISH -> R.id.language_spanish
                AppSettings.LANGUAGE_FRENCH -> R.id.language_french
                AppSettings.LANGUAGE_GERMAN -> R.id.language_german
                AppSettings.LANGUAGE_PORTUGUESE -> R.id.language_portuguese
                AppSettings.LANGUAGE_HINDI -> R.id.language_hindi
                AppSettings.LANGUAGE_CHINESE -> R.id.language_chinese
                AppSettings.LANGUAGE_JAPANESE -> R.id.language_japanese
                AppSettings.LANGUAGE_KOREAN -> R.id.language_korean
                AppSettings.LANGUAGE_TURKISH -> R.id.language_turkish
                AppSettings.LANGUAGE_UKRAINIAN -> R.id.language_ukrainian
                else -> R.id.language_system
            }
        )
    }

    private fun setupPicker() {
        binding.languageGroup.setOnCheckedChangeListener { _, checkedId ->
            val language = when (checkedId) {
                R.id.language_english -> AppSettings.LANGUAGE_ENGLISH
                R.id.language_armenian -> AppSettings.LANGUAGE_ARMENIAN
                R.id.language_russian -> AppSettings.LANGUAGE_RUSSIAN
                R.id.language_arabic -> AppSettings.LANGUAGE_ARABIC
                R.id.language_persian -> AppSettings.LANGUAGE_PERSIAN
                R.id.language_spanish -> AppSettings.LANGUAGE_SPANISH
                R.id.language_french -> AppSettings.LANGUAGE_FRENCH
                R.id.language_german -> AppSettings.LANGUAGE_GERMAN
                R.id.language_portuguese -> AppSettings.LANGUAGE_PORTUGUESE
                R.id.language_hindi -> AppSettings.LANGUAGE_HINDI
                R.id.language_chinese -> AppSettings.LANGUAGE_CHINESE
                R.id.language_japanese -> AppSettings.LANGUAGE_JAPANESE
                R.id.language_korean -> AppSettings.LANGUAGE_KOREAN
                R.id.language_turkish -> AppSettings.LANGUAGE_TURKISH
                R.id.language_ukrainian -> AppSettings.LANGUAGE_UKRAINIAN
                else -> AppSettings.LANGUAGE_SYSTEM
            }
            viewModel.updateLanguage(language)
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
