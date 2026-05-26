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
import com.codestream.tetrak.premium.PremiumBillingManager
import com.codestream.tetrak.utils.AppSettings
import com.google.android.material.snackbar.Snackbar

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
        setupPremiumControls()
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

    private fun setupPremiumControls() = with(binding) {
        refreshPremiumUi()

        premiumUpgradeBtn.setOnClickListener {
            it.animate().scaleX(0.96f).scaleY(0.96f).setDuration(70L).withEndAction {
                it.animate().scaleX(1f).scaleY(1f).setDuration(120L).start()
            }.start()
            PremiumBillingManager.launchPremiumPurchase(requireActivity()) { message ->
                Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
            }
        }

        premiumRestoreBtn.setOnClickListener {
            PremiumBillingManager.restorePurchases(requireContext()) { _, message ->
                refreshPremiumUi()
                Snackbar.make(root, message, Snackbar.LENGTH_LONG).show()
            }
        }

        premiumDebugSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setPremiumForDevelopment(isChecked)
            refreshPremiumUi()
            Snackbar.make(
                root,
                if (isChecked) R.string.premium_dev_enabled else R.string.premium_dev_disabled,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }

    private fun refreshPremiumUi() = with(binding) {
        val premium = viewModel.isPremium()
        premiumStatus.setText(if (premium) R.string.premium_status_active else R.string.premium_status_free)
        premiumDebugSwitch.isChecked = premium
        adsCard.alpha = if (premium) 0.55f else 1f
        premiumUpgradeBtn.isEnabled = !premium
    }

    private fun animateIntro() = with(binding) {
        themeCard.translationY = 32f
        languageCard.translationY = 32f
        premiumCard.translationY = 32f
        adsCard.translationY = 32f
        themeCard.alpha = 0f
        languageCard.alpha = 0f
        premiumCard.alpha = 0f
        adsCard.alpha = 0f
        themeCard.animate().translationY(0f).alpha(1f).setDuration(280L).start()
        languageCard.animate().translationY(0f).alpha(1f).setStartDelay(90L).setDuration(280L).start()
        premiumCard.animate().translationY(0f).alpha(1f).setStartDelay(180L).setDuration(280L).start()
        adsCard.animate().translationY(0f).alpha(if (viewModel.isPremium()) 0.55f else 1f).setStartDelay(270L).setDuration(280L).start()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
