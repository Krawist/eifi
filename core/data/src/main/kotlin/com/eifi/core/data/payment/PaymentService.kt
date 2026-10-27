package com.eifi.core.data.payment

import com.eifi.core.data.ReceivingProfile
import com.eifi.core.payload.PaymentRequest
import com.eifi.core.payload.PaymentRequestService
import java.time.Duration
import java.time.Instant
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Lifecycle of a [Payment].
 *
 * CONFIRMED and CANCELLED are final. EXPIRED is final too, except for the operator's SMS arriving less than
 * [LATE_CONFIRMATION_WINDOW] after the expiry: the payer paid a little late and the money did arrive (EIFI-106).
 * Repeating a transition already done changes nothing; any other transition from a final state is refused.
 */
class PaymentService @Inject constructor() {

    /** A new PENDING received payment of [amount], waiting [DEFAULT_DURATION] from [at]. */
    fun newReceived(amount: Long, label: String?, at: Instant): Payment {
        require(amount > 0) { "The amount must be positive" }
        return Payment(
            id = PaymentRequestService.newId(),
            type = TransactionType.Received,
            amount = amount,
            label = label,
            createdAt = at,
            expiresAt = at.plus(DEFAULT_DURATION),
        )
    }

    /** A new PENDING sent payment of [request], paid at [at]; it keeps the id of the request, see [Payment.id]. */
    fun newSent(request: PaymentRequest, at: Instant): Payment = Payment(
        id = request.id,
        type = TransactionType.Sent,
        amount = request.amount,
        label = null,
        createdAt = at,
        expiresAt = request.expiresAt,
    )

    /** The request carried by the payment URL of a received [payment], to be paid to [profile]. */
    fun toPaymentRequest(payment: Payment, profile: ReceivingProfile): PaymentRequest = PaymentRequest(
        id = payment.id,
        displayName = profile.displayName,
        amount = payment.amount,
        receivingMethods = profile.receivingMethods,
        // The URL carries whole seconds: rounded down, so that the payer never sees a later expiry.
        expiresAt = payment.expiresAt.truncatedTo(ChronoUnit.SECONDS),
    )

    /** The status at [now]: a PENDING payment past its expiry is EXPIRED, even before it is stored as such. */
    fun statusAt(payment: Payment, now: Instant): PaymentStatus =
        if (payment.status == PaymentStatus.Pending && !now.isBefore(payment.expiresAt)) {
            PaymentStatus.Expired
        } else {
            payment.status
        }

    /** Confirmed at [at]; an expired payment only by [ConfirmationSource.Sms] within the late window. */
    fun confirm(payment: Payment, source: ConfirmationSource, at: Instant): Payment {
        val current = statusAt(payment, at)
        if (current == PaymentStatus.Confirmed) return payment
        val lateSms = current == PaymentStatus.Expired &&
            source == ConfirmationSource.Sms &&
            at.isBefore(payment.expiresAt.plus(LATE_CONFIRMATION_WINDOW))
        check(current == PaymentStatus.Pending || lateSms) { "A $current payment cannot be confirmed by $source" }
        return payment.copy(status = PaymentStatus.Confirmed, confirmationSource = source, endedAt = at)
    }

    /** Cancelled at [at]. */
    fun cancel(payment: Payment, at: Instant): Payment {
        val current = statusAt(payment, at)
        if (current == PaymentStatus.Cancelled) return payment
        check(current == PaymentStatus.Pending) { "A $current payment cannot be cancelled" }
        return payment.copy(status = PaymentStatus.Cancelled, endedAt = at)
    }

    /** Stored as EXPIRED once [at] has reached the expiry; ended at the expiry, not when it was noticed. */
    fun expire(payment: Payment, at: Instant): Payment {
        val current = statusAt(payment, at)
        if (payment.status == PaymentStatus.Expired) return payment
        check(current == PaymentStatus.Expired) { "A $current payment cannot expire at $at" }
        return payment.copy(status = PaymentStatus.Expired, endedAt = payment.expiresAt)
    }

    companion object {
        /** How long a payment waits (M4: configurable, 2 minutes by default). */
        val DEFAULT_DURATION: Duration = Duration.ofMinutes(2)

        /** How long after the expiry the operator's SMS can still confirm the payment. */
        val LATE_CONFIRMATION_WINDOW: Duration = Duration.ofMinutes(10)
    }
}
