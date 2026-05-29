package com.codestream.tetrak.utils

object AppConstants {
    const val DATABASE_VERSION = 3

    /**
     * Global feature flag for the whole Premium layer.
     *
     * true  -> show Premium UI, allow billing flow, gate Premium-only features, hide ads for Premium users.
     * false -> hide Premium UI and disable Premium-only entry points completely.
     *
     * Change only this constant when you want to build a version without Premium subscriptions.
     */
    const val HAS_PREMIUM_FEATURES = true
}
