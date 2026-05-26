package com.codestream.tetrak.premium

import android.content.Context

object PremiumManager {
    private const val PREFS = "tetrak_premium_prefs"
    private const val KEY_IS_PREMIUM = "is_premium"
    private const val KEY_ACTIVE_PRODUCT_ID = "active_product_id"
    private const val KEY_PURCHASE_TOKEN = "purchase_token"
    private const val KEY_AI_TITLE_CLICKS = "ai_title_clicks"

    fun isPremium(context: Context): Boolean = prefs(context).getBoolean(KEY_IS_PREMIUM, false)

    fun activeProductId(context: Context): String? =
        prefs(context).getString(KEY_ACTIVE_PRODUCT_ID, null)

    fun setPremiumEntitlement(context: Context, productId: String?, purchaseToken: String?) {
        prefs(context).edit()
            .putBoolean(KEY_IS_PREMIUM, productId != null)
            .putString(KEY_ACTIVE_PRODUCT_ID, productId)
            .putString(KEY_PURCHASE_TOKEN, purchaseToken)
            .apply()
    }

    fun setPremiumForDevelopment(context: Context, enabled: Boolean) {
        setPremiumEntitlement(
            context = context,
            productId = if (enabled) PremiumConfig.PREMIUM_MONTHLY_PRODUCT_ID else null,
            purchaseToken = if (enabled) "development_unlock" else null
        )
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
