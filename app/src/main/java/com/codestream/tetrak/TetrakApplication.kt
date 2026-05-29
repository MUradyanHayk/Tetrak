package com.codestream.tetrak

import android.app.Application
import com.codestream.tetrak.ads.AdMobManager
import com.codestream.tetrak.premium.PremiumBillingManager
import com.codestream.tetrak.utils.AppConstants

class TetrakApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdMobManager.initialize(this)
        if (AppConstants.HAS_PREMIUM_FEATURES) {
            PremiumBillingManager.initialize(this)
        }
    }
}
