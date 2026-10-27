package com.eifi.core.data

import com.eifi.core.payload.Operator
import kotlinx.serialization.Serializable

/**
 * The payer's account remembered for the next payments. Stored as JSON (see [JsonSerializer]): a new property needs
 * a default value.
 *
 * @property operator the mobile money operator of the account.
 * @property subscriptionId Android subscription id of the SIM used, `null` when the SIMs were not known (phone calls
 * permission refused).
 */
@Serializable
data class PayingAccountChoice(val operator: Operator, val subscriptionId: Int?)
