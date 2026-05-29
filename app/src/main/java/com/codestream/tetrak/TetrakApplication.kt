package com.codestream.tetrak

import android.app.Application
import com.codestream.tetrak.ads.AdMobManager
import com.codestream.tetrak.db.NoteDatabase
import com.codestream.tetrak.premium.PremiumBillingManager
import com.codestream.tetrak.utils.AppConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TetrakApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdMobManager.initialize(this)
        if (AppConstants.HAS_PREMIUM_FEATURES) {
            PremiumBillingManager.initialize(this)
        }
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            NoteDatabase.getInstance(this@TetrakApplication)
                .getNoteDao()
                .deleteExpiredDeletedNotes(System.currentTimeMillis())
        }
    }
}
