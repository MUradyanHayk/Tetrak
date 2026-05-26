package com.codestream.tetrak.premium

object PremiumConfig {
    // Create these two subscription products in Google Play Console before production release.
    // Recommended public prices:
    // - Monthly: 1.99 USD
    // - Yearly: 11.99 USD
    const val PREMIUM_MONTHLY_PRODUCT_ID = "tetrak_premium_monthly"
    const val PREMIUM_YEARLY_PRODUCT_ID = "tetrak_premium_yearly"

    val PRODUCT_IDS = listOf(PREMIUM_MONTHLY_PRODUCT_ID, PREMIUM_YEARLY_PRODUCT_ID)

    const val DEFAULT_MONTHLY_PRICE = "$1.99"
    const val DEFAULT_YEARLY_PRICE = "$11.99"
}
