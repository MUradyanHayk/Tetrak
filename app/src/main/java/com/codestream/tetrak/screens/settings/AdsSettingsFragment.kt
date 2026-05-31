package com.codestream.tetrak.screens.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentAdsSettingsBinding

class AdsSettingsFragment : Fragment() {
    private var _binding: FragmentAdsSettingsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentAdsSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.toolbar.setNavigationOnClickListener { findNavController().navigateUp() }
        binding.adsStatus.text = getString(if (viewModel.isPremium()) R.string.ads_disabled_for_premium else R.string.ads_enabled_for_free)
        binding.adsDescription.setText(if (viewModel.isPremium()) R.string.ads_premium_disabled_message else R.string.ads_info)
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
