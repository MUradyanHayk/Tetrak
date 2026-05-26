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
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import java.util.concurrent.atomic.AtomicBoolean

object AdMobManager {
    private val initialized = AtomicBoolean(false)

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
