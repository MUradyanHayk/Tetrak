package com.codestream.tetrak.premium

import android.app.Activity
import android.content.Context

/**
 * Premium billing boundary.
 *
 * This keeps the app architecture clean today and gives you one file to connect
 * to Google Play Billing after the subscription product is created in Play Console.
 * Production flow should be:
 * 1) Query product details for PremiumConfig.PREMIUM_SUBSCRIPTION_PRODUCT_ID.
 * 2) Launch Google Play Billing subscription flow.
 * 3) Verify purchase on your backend.
 * 4) Store the final entitlement with PremiumManager.setPremiumForDevelopment(context, true)
 *    or replace that method with a secure entitlement cache from your backend.
 */
object PremiumBillingManager {
    fun launchPremiumPurchase(activity: Activity, onUnavailable: (String) -> Unit) {
        onUnavailable(
            activity.getString(
                com.codestream.tetrak.R.string.premium_billing_not_configured,
                PremiumConfig.PREMIUM_SUBSCRIPTION_PRODUCT_ID
            )
        )
    }

    fun restorePurchases(context: Context, onResult: (Boolean, String) -> Unit) {
        val premium = PremiumManager.isPremium(context)
        onResult(
            premium,
            context.getString(if (premium) com.codestream.tetrak.R.string.premium_restore_success else com.codestream.tetrak.R.string.premium_restore_empty)
        )
    }
}
