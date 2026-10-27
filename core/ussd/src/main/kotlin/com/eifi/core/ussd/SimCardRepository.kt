package com.eifi.core.ussd

/** The SIM cards of the phone. */
interface SimCardRepository {
    /** The active SIM cards, by slot; `null` without the phone calls permission (`READ_PHONE_STATE`). */
    fun activeSimCards(): List<SimCard>?
}
