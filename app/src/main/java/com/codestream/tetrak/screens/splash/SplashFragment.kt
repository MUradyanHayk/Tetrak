package com.codestream.tetrak.screens.splash

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.codestream.tetrak.R
import com.codestream.tetrak.databinding.FragmentSplashBinding
import com.codestream.tetrak.utils.AppSettings

class SplashFragment : Fragment() {
    private var _binding: FragmentSplashBinding? = null
    private val binding get() = _binding!!
    private val openAppRunnable = Runnable { openApp() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSplashBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playIntroAnimation()
        view.postDelayed(openAppRunnable, SPLASH_DURATION_MS)
    }

    private fun playIntroAnimation() = with(binding) {
        logoImage.alpha = 0f
        logoImage.scaleX = 0.82f
        logoImage.scaleY = 0.82f
        appNameText.alpha = 0f
        appNameText.translationY = 18f

        logoImage.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(520L)
            .start()

        appNameText.animate()
            .alpha(1f)
            .translationY(0f)
            .setStartDelay(160L)
            .setDuration(420L)
            .start()
    }

    private fun openApp() {
        if (!isAdded) return
        AppSettings.markFirstSplashSeen(requireContext())
        val options = NavOptions.Builder()
            .setPopUpTo(R.id.splashFragment, true)
            .setLaunchSingleTop(true)
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .build()
        findNavController().navigate(R.id.startFragment, null, options)
    }

    override fun onDestroyView() {
        binding.root.removeCallbacks(openAppRunnable)
        _binding = null
        super.onDestroyView()
    }

    private companion object {
        const val SPLASH_DURATION_MS = 1400L
    }
}
