package com.eifi.core.data

import com.eifi.core.payload.PaymentRequestService
import com.eifi.core.payload.ReceivingMethodService
import javax.inject.Inject

/** Rules of a [ReceivingProfile]: what a payment URL needs from it. */
class ReceivingProfileService @Inject constructor() {

    /** @throws IllegalArgumentException when [profile] could not be put in a payment URL. */
    fun validate(profile: ReceivingProfile) {
        require(PaymentRequestService.isValidDisplayName(profile.displayName)) {
            "The display name must have 1 to ${PaymentRequestService.MAX_DISPLAY_NAME_LENGTH} characters"
        }
        require(profile.receivingMethods.isNotEmpty()) { "At least one operator is required" }
        profile.receivingMethods.values.forEach(ReceivingMethodService::validate)
    }
}
