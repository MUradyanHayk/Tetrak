package com.codestream.tetrak.premium

data class PremiumPlan(
    val productId: String,
    val title: String,
    val subtitle: String,
    val formattedPrice: String,
    val offerToken: String? = null,
    val isBestValue: Boolean = false
)
