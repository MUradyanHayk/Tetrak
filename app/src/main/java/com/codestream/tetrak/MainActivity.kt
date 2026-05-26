package com.codestream.tetrak

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import com.codestream.tetrak.ads.AdMobManager
import com.codestream.tetrak.databinding.ActivityMainBinding
import com.codestream.tetrak.utils.AppSettings

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var bannerAdView: com.google.android.gms.ads.AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AppSettings.applySavedSettings(this)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            animateAdForKeyboard(imeVisible)
            insets
        }
        setupAdMobBanner()
    }

    private fun setupAdMobBanner() {
        AdMobManager.loadAdaptiveBanner(
            activity = this,
            adContainer = binding.adViewContainer,
            adShell = binding.adSection,
            loadingView = binding.adLoading
        ) { adView ->
            bannerAdView = adView
        }
    }

    private fun animateAdForKeyboard(keyboardVisible: Boolean) {
        val targetAlpha = if (keyboardVisible) 0f else 1f
        val targetTranslation = if (keyboardVisible) binding.adSection.height.coerceAtLeast(64).toFloat() else 0f
        binding.adSection.animate()
            .alpha(targetAlpha)
            .translationY(targetTranslation)
            .setDuration(180L)
            .withStartAction { if (!keyboardVisible && bannerAdView != null) binding.adSection.isVisible = true }
            .withEndAction { if (keyboardVisible) binding.adSection.isVisible = false }
            .start()
    }

    override fun onResume() {
        super.onResume()
        bannerAdView?.resume()
    }

    override fun onPause() {
        bannerAdView?.pause()
        super.onPause()
    }

    override fun onDestroy() {
        bannerAdView?.destroy()
        bannerAdView = null
        super.onDestroy()
    }
}
