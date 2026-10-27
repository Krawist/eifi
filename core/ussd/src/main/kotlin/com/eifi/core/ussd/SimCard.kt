package com.eifi.core.ussd

import com.eifi.core.payload.Operator

/**
 * An active SIM card of the phone.
 *
 * @property subscriptionId Android subscription id, to place the USSD call on this SIM (EIFI-204).
 * @property slotIndex SIM slot, from 0; shown from 1 ("SIM 1").
 * @property carrierName name of the network shown by the phone, e.g. "Orange CM".
 * @property operator the mobile money operator of the SIM, `null` when its network is not one Eifi supports.
 */
data class SimCard(val subscriptionId: Int, val slotIndex: Int, val carrierName: String, val operator: Operator?)
