package com.codestream.tetrak.premium

import android.content.Context

object PremiumManager {
    private const val PREFS = "tetrak_premium_prefs"
    private const val KEY_IS_PREMIUM = "is_premium"
    private const val KEY_AI_TITLE_CLICKS = "ai_title_clicks"

    fun isPremium(context: Context): Boolean = prefs(context).getBoolean(KEY_IS_PREMIUM, false)

    fun setPremiumForDevelopment(context: Context, enabled: Boolean) {
        prefs(context).edit().putBoolean(KEY_IS_PREMIUM, enabled).apply()
    }

    fun recordAiTitleClick(context: Context): Int {
        val nextCount = prefs(context).getInt(KEY_AI_TITLE_CLICKS, 0) + 1
        prefs(context).edit().putInt(KEY_AI_TITLE_CLICKS, nextCount).apply()
        return nextCount
    }

    fun aiTitleClickCount(context: Context): Int = prefs(context).getInt(KEY_AI_TITLE_CLICKS, 0)

    private fun prefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
