package com.eifi.core.ussd

/** Launches a USSD code. */
interface UssdLauncher {
    /**
     * Calls [code] on the SIM of [subscriptionId] (the phone picks it when `null` or not found); `false` without
     * the phone calls permission, when nothing is called.
     */
    fun call(code: String, subscriptionId: Int?): Boolean

    /** Opens the dialer with [code] typed, for the payer to call it themselves. */
    fun dial(code: String)
}
