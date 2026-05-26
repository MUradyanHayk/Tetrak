package com.codestream.tetrak.ads

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.core.view.doOnLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import java.util.concurrent.atomic.AtomicBoolean

object AdMobManager {
    private const val PREFS = "tetrak_admob_prefs"
    private const val KEY_NOTES_SAVED_SINCE_VIDEO_AD = "notes_saved_since_video_ad"
    private const val KEY_LAST_VIDEO_AD_TIME = "last_video_ad_time"
    private const val VIDEO_AD_SAVE_INTERVAL = 3
    private const val VIDEO_AD_COOLDOWN_MS = 2 * 60 * 1000L

    private val initialized = AtomicBoolean(false)
    private var interstitialAd: InterstitialAd? = null
    private var interstitialLoading = false

    fun initialize(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        val appContext = context.applicationContext
        Thread { MobileAds.initialize(appContext) {} }.start()
    }

    fun loadAdaptiveBanner(
        activity: Activity,
        adContainer: FrameLayout,
        adShell: View,
        loadingView: ProgressBar,
        onLoaded: (AdView) -> Unit
    ) {
        initialize(activity)
        adShell.showAnimated()
        loadingView.visibility = View.VISIBLE

        adContainer.doOnLayout {
            if (adContainer.childCount > 0) return@doOnLayout

            val adWidth = calculateAdWidth(activity, adContainer)
            val adView = AdView(activity).apply {
                adUnitId = AdMobConfig.BANNER_AD_UNIT_ID
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, adWidth))
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        loadingView.animate().alpha(0f).setDuration(150L).withEndAction {
                            loadingView.visibility = View.GONE
                            loadingView.alpha = 1f
                        }.start()
                        adShell.showAnimated()
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        loadingView.visibility = View.GONE
                        adShell.hideAnimated()
                    }
                }
            }

            adContainer.removeAllViews()
            adContainer.addView(
                adView,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            )
            adView.loadAd(AdRequest.Builder().build())
            onLoaded(adView)
        }
    }

    fun preloadVideoAd(context: Context) {
        initialize(context)
        if (interstitialAd != null || interstitialLoading) return

        interstitialLoading = true
        InterstitialAd.load(
            context.applicationContext,
            AdMobConfig.INTERSTITIAL_VIDEO_AD_UNIT_ID,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    interstitialLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    interstitialLoading = false
                }
            }
        )
    }

    fun recordNoteSavedAndMaybeShowVideoAd(
        activity: Activity,
        onFinished: () -> Unit
    ) {
        initialize(activity)

        val prefs = activity.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val savedCount = prefs.getInt(KEY_NOTES_SAVED_SINCE_VIDEO_AD, 0) + 1
        val now = System.currentTimeMillis()
        val lastAdTime = prefs.getLong(KEY_LAST_VIDEO_AD_TIME, 0L)
        val cooldownPassed = now - lastAdTime >= VIDEO_AD_COOLDOWN_MS
        val shouldShow = savedCount >= VIDEO_AD_SAVE_INTERVAL && cooldownPassed
        val ad = interstitialAd

        if (!shouldShow || ad == null) {
            prefs.edit().putInt(KEY_NOTES_SAVED_SINCE_VIDEO_AD, savedCount).apply()
            preloadVideoAd(activity)
            onFinished()
            return
        }

        prefs.edit()
            .putInt(KEY_NOTES_SAVED_SINCE_VIDEO_AD, 0)
            .putLong(KEY_LAST_VIDEO_AD_TIME, now)
            .apply()

        interstitialAd = null
        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                preloadVideoAd(activity)
                onFinished()
            }

            override fun onAdFailedToShowFullScreenContent(error: com.google.android.gms.ads.AdError) {
                preloadVideoAd(activity)
                onFinished()
            }
        }
        ad.show(activity)
    }

    private fun calculateAdWidth(activity: Activity, container: View): Int {
        val displayMetrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        activity.windowManager.defaultDisplay.getMetrics(displayMetrics)
        val density = displayMetrics.density
        val widthPixels = if (container.width > 0) container.width else displayMetrics.widthPixels
        return (widthPixels / density).toInt().coerceAtLeast(320)
    }

    private fun View.showAnimated() {
        if (visibility != View.VISIBLE) {
            visibility = View.VISIBLE
            alpha = 0f
            translationY = height.coerceAtLeast(24).toFloat()
        }
        animate().alpha(1f).translationY(0f).setDuration(220L).start()
    }

    private fun View.hideAnimated() {
        animate().alpha(0f).translationY(height.coerceAtLeast(24).toFloat()).setDuration(180L).withEndAction {
            visibility = View.GONE
            translationY = 0f
        }.start()
    }
}
