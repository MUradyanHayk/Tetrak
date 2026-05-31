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
import com.codestream.tetrak.utils.AppConstants

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
        setupSectionClicks()
        bindDynamicState()
        animateIntro()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
    }

    private fun setupSectionClicks() = with(binding) {
        languageSection.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_languageSettingsFragment) }
        themeSection.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_themeSettingsFragment) }
        premiumSection.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_premiumFragment) }
        adsSection.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_adsSettingsFragment) }
        deletedNotesSection.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_deletedNotesFragment) }
        appInfoSection.setOnClickListener { findNavController().navigate(R.id.action_settingsFragment_to_appInfoSettingsFragment) }

        if (!AppConstants.HAS_PREMIUM_FEATURES) {
            premiumSection.visibility = View.GONE
        }
    }

    private fun bindDynamicState() = with(binding) {
        languageValue.text = viewModel.languageLabel(requireContext())
        themeValue.text = viewModel.themeLabel(requireContext())
        premiumValue.text = getString(if (viewModel.isPremium()) R.string.premium_status_active else R.string.premium_status_free)
        adsValue.text = getString(if (viewModel.isPremium()) R.string.ads_disabled_for_premium else R.string.ads_enabled_for_free)
        viewModel.deletedNotes.observe(viewLifecycleOwner) { notes ->
            deletedNotesValue.text = resources.getQuantityString(
                R.plurals.deleted_notes_count,
                notes.size,
                notes.size
            )
        }
    }

    private fun animateIntro() {
        val sections = listOf(
            binding.languageSection,
            binding.themeSection,
            binding.premiumSection,
            binding.adsSection,
            binding.deletedNotesSection,
            binding.appInfoSection
        ).filter { it.visibility == View.VISIBLE }

        sections.forEachIndexed { index, view ->
            view.alpha = 0f
            view.translationY = 28f
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setStartDelay(index * 55L)
                .setDuration(260L)
                .start()
        }
    }

    override fun onResume() {
        super.onResume()
        if (_binding != null) bindDynamicState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
