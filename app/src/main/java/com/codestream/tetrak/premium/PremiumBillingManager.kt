package com.codestream.tetrak.premium

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.codestream.tetrak.R
import com.codestream.tetrak.utils.AppConstants
import com.codestream.tetrak.utils.toPremiumPlan

/**
 * Real Google Play Billing boundary for Tetrak Premium.
 *
 * Product setup in Play Console:
 * - tetrak_premium_monthly: subscription, recommended price $1.99/month
 * - tetrak_premium_yearly: subscription, recommended price $11.99/year
 *
 * Production note: client-side acknowledgement is implemented so testing works.
 * For a real store release, verify purchase tokens on your backend before granting
 * permanent entitlement, then cache the entitlement locally with PremiumManager.
 */
object PremiumBillingManager : PurchasesUpdatedListener {
    private var billingClient: BillingClient? = null
    private var appContext: Context? = null
    private var isConnecting = false
    private val pendingConnectionCallbacks = mutableListOf<() -> Unit>()
    private val productDetails = linkedMapOf<String, ProductDetails>()

    fun initialize(context: Context) {
        if (!AppConstants.HAS_PREMIUM_FEATURES) return
        appContext = context.applicationContext
        if (billingClient != null) return
        billingClient = BillingClient.newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .enableAutoServiceReconnection()
            .build()
        connectIfNeeded()
    }

    fun loadPlans(context: Context, onResult: (List<PremiumPlan>, String?) -> Unit) {
        if (!AppConstants.HAS_PREMIUM_FEATURES) {
            onResult(emptyList(), null)
            return
        }
        initialize(context)
        connectIfNeeded {
            val client = billingClient
            if (client == null || !client.isReady) {
                onResult(defaultPlans(context), context.getString(R.string.premium_billing_unavailable))
                return@connectIfNeeded
            }

            val products = PremiumConfig.PRODUCT_IDS.map { productId ->
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            }
            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(products)
                .build()

            client.queryProductDetailsAsync(params) { billingResult, queryResult ->
                val detailsList = queryResult.productDetailsList
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && detailsList.isNotEmpty()) {
                    productDetails.clear()
                    detailsList.forEach { productDetails[it.productId] = it }
                    onResult(detailsList.map { it.toPremiumPlan(context) }.sortedBy { it.productId }, null)
                } else {
                    onResult(defaultPlans(context), billingResult.debugMessage.ifBlank { context.getString(R.string.premium_products_not_ready) })
                }
            }
        }
    }

    fun launchPremiumPurchase(activity: Activity, productId: String, onResult: (String) -> Unit) {
        if (!AppConstants.HAS_PREMIUM_FEATURES) {
            onResult(activity.getString(R.string.premium_billing_unavailable))
            return
        }
        initialize(activity)
        connectIfNeeded {
            val client = billingClient
            val details = productDetails[productId]
            if (client == null || !client.isReady || details == null) {
                onResult(activity.getString(R.string.premium_products_not_ready))
                return@connectIfNeeded
            }

            val offerToken = details.subscriptionOfferDetails?.firstOrNull()?.offerToken
            val productParamsBuilder = BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(details)
            if (!offerToken.isNullOrBlank()) productParamsBuilder.setOfferToken(offerToken)

            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(productParamsBuilder.build()))
                .build()
            val result = client.launchBillingFlow(activity, flowParams)
            if (result.responseCode != BillingClient.BillingResponseCode.OK) {
                onResult(result.debugMessage.ifBlank { activity.getString(R.string.premium_billing_unavailable) })
            }
        }
    }

    fun restorePurchases(context: Context, onResult: (Boolean, String) -> Unit) {
        if (!AppConstants.HAS_PREMIUM_FEATURES) {
            onResult(false, context.getString(R.string.premium_billing_unavailable))
            return
        }
        initialize(context)
        connectIfNeeded {
            val client = billingClient
            if (client == null || !client.isReady) {
                onResult(false, context.getString(R.string.premium_billing_unavailable))
                return@connectIfNeeded
            }
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            client.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    val activePurchase = purchases.firstOrNull { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                    if (activePurchase != null) {
                        grantPurchase(context, activePurchase)
                        onResult(true, context.getString(R.string.premium_restore_success))
                    } else {
                        PremiumManager.setPremiumEntitlement(context, null, null)
                        onResult(false, context.getString(R.string.premium_restore_empty))
                    }
                } else {
                    onResult(false, billingResult.debugMessage.ifBlank { context.getString(R.string.premium_restore_empty) })
                }
            }
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        val context = appContext ?: return
        when (billingResult.responseCode) {
            BillingClient.BillingResponseCode.OK -> purchases.orEmpty().forEach { grantPurchase(context, it) }
            BillingClient.BillingResponseCode.USER_CANCELED -> Unit
            else -> Unit
        }
    }

    private fun grantPurchase(context: Context, purchase: Purchase) {
        if (purchase.purchaseState != Purchase.PurchaseState.PURCHASED) return
        val productId = purchase.products.firstOrNull { it in PremiumConfig.PRODUCT_IDS } ?: return
        PremiumManager.setPremiumEntitlement(context, productId, purchase.purchaseToken)

        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient?.acknowledgePurchase(params) { /* entitlement already cached for UX */ }
        }
    }

    private fun connectIfNeeded(onConnected: (() -> Unit)? = null) {
        val client = billingClient ?: return
        if (client.isReady) {
            onConnected?.invoke()
            return
        }
        onConnected?.let { pendingConnectionCallbacks.add(it) }
        if (isConnecting) return
        isConnecting = true
        client.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                isConnecting = false
                val callbacks = pendingConnectionCallbacks.toList()
                pendingConnectionCallbacks.clear()
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    callbacks.forEach { it.invoke() }
                }
            }

            override fun onBillingServiceDisconnected() {
                isConnecting = false
            }
        })
    }

    fun defaultPlans(context: Context): List<PremiumPlan> = listOf(
        PremiumPlan(
            productId = PremiumConfig.PREMIUM_MONTHLY_PRODUCT_ID,
            title = context.getString(R.string.premium_monthly),
            subtitle = context.getString(R.string.premium_monthly_subtitle),
            formattedPrice = PremiumConfig.DEFAULT_MONTHLY_PRICE,
            isBestValue = false
        ),
        PremiumPlan(
            productId = PremiumConfig.PREMIUM_YEARLY_PRODUCT_ID,
            title = context.getString(R.string.premium_yearly),
            subtitle = context.getString(R.string.premium_yearly_subtitle),
            formattedPrice = PremiumConfig.DEFAULT_YEARLY_PRICE,
            isBestValue = true
        )
    )
}
