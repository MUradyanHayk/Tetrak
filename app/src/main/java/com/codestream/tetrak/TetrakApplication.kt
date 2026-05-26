package com.codestream.tetrak

import android.app.Application
import com.codestream.tetrak.ads.AdMobManager

class TetrakApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdMobManager.initialize(this)
    }
}
