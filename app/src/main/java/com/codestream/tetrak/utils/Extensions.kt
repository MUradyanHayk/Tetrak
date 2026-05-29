package com.codestream.tetrak.utils

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.util.DisplayMetrics
import android.view.View
import com.android.billingclient.api.ProductDetails
import com.codestream.tetrak.R
import com.codestream.tetrak.premium.PremiumConfig
import com.codestream.tetrak.premium.PremiumPlan
import kotlin.math.roundToInt

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).roundToInt()

val Float.dp: Float
    get() = this * Resources.getSystem().displayMetrics.density

fun Int.pxToDp(displayMetrics: DisplayMetrics = Resources.getSystem().displayMetrics): Int =
    (this / displayMetrics.density).roundToInt()

fun Int.toHexColor(): String = String.format("#%06X", 0xFFFFFF and this)

fun String.toColorOrNull(): Int? {
    val normalized = trim().removePrefix("#")
    if (!Regex("^[0-9A-Fa-f]{6}$").matches(normalized)) return null
    return runCatching { Color.parseColor("#$normalized") }.getOrNull()
}

fun View.showAnimated() {
    if (visibility != View.VISIBLE) {
        visibility = View.VISIBLE
        alpha = 0f
        translationY = height.coerceAtLeast(24).toFloat()
    }
    animate().alpha(1f).translationY(0f).setDuration(220L).start()
}

fun View.hideAnimated() {
    animate()
        .alpha(0f)
        .translationY(height.coerceAtLeast(24).toFloat())
        .setDuration(180L)
        .withEndAction {
            visibility = View.GONE
            translationY = 0f
        }
        .start()
}

fun ProductDetails.toPremiumPlan(context: Context): PremiumPlan {
    val price = subscriptionOfferDetails
        ?.firstOrNull()
        ?.pricingPhases
        ?.pricingPhaseList
        ?.firstOrNull()
        ?.formattedPrice
        ?: if (productId == PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) {
            PremiumConfig.DEFAULT_YEARLY_PRICE
        } else {
            PremiumConfig.DEFAULT_MONTHLY_PRICE
        }

    return PremiumPlan(
        productId = productId,
        title = if (productId == PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) {
            context.getString(R.string.premium_yearly)
        } else {
            context.getString(R.string.premium_monthly)
        },
        subtitle = if (productId == PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID) {
            context.getString(R.string.premium_yearly_subtitle)
        } else {
            context.getString(R.string.premium_monthly_subtitle)
        },
        formattedPrice = price,
        offerToken = subscriptionOfferDetails?.firstOrNull()?.offerToken,
        isBestValue = productId == PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID
    )
}
