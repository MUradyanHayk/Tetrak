package com.codestream.tetrak

import android.app.Application
import com.codestream.tetrak.ads.AdMobManager
import com.codestream.tetrak.premium.PremiumBillingManager

class TetrakApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdMobManager.initialize(this)
        PremiumBillingManager.initialize(this)
    }
}
