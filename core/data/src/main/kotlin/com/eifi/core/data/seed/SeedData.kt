package com.eifi.core.data.seed

import com.eifi.core.data.payment.ConfirmationSource
import com.eifi.core.data.payment.Payment
import com.eifi.core.data.payment.PaymentStatus
import com.eifi.core.data.payment.TransactionType
import com.eifi.core.payload.Operator
import com.eifi.core.ussd.OperatorUssdConfig
import java.time.Duration
import java.time.Instant

/**
 * What [DatabaseSeeder] preloads in an empty database. TODO(EIFI-301): remove the sample payments after the pilot; the
 * USSD codes will come from the backend.
 */
internal object SeedData {
    /** The merchant payment codes validated by Krawist (EIFI-001). TODO(EIFI-001): the transfer codes. */
    val ussdConfigs = listOf(
        OperatorUssdConfig(
            operator = Operator.OrangeMoney,
            merchantPayment = "#150*47*{code}*{amount}#",
            transfer = null,
        ),
        OperatorUssdConfig(operator = Operator.MtnMomo, merchantPayment = "*126*2*{code}*{amount}#", transfer = null),
    )

    /** Sample payments received and sent before [now], for the demonstrations of the pilot. */
    fun payments(now: Instant): List<Payment> = listOf(
        Payment(
            id = "5eed00000001",
            type = TransactionType.Received,
            amount = 2_500,
            label = null,
            createdAt = now.minus(Duration.parse("PT25M")),
            expiresAt = now.minus(Duration.parse("PT23M")),
            status = PaymentStatus.Confirmed,
            confirmationSource = ConfirmationSource.Sms,
            endedAt = now.minus(Duration.parse("PT24M")),
            counterpartyName = "+237 6XX XX XX 41",
            operator = Operator.OrangeMoney,
        ),
        Payment(
            id = "5eed00000002",
            type = TransactionType.Received,
            amount = 5_000,
            label = "Riz",
            createdAt = now.minus(Duration.parse("PT3H")),
            expiresAt = now.minus(Duration.parse("PT2H58M")),
            status = PaymentStatus.Confirmed,
            confirmationSource = ConfirmationSource.Manual,
            endedAt = now.minus(Duration.parse("PT2H59M")),
            counterpartyName = "+237 6XX XX XX 07",
            operator = Operator.MtnMomo,
        ),
        Payment(
            id = "5eed00000003",
            type = TransactionType.Sent,
            amount = 1_000,
            label = null,
            createdAt = now.minus(Duration.parse("P1D")),
            expiresAt = now.minus(Duration.parse("PT23H58M")),
            counterpartyName = "Boutique Mama Ngono",
            operator = Operator.MtnMomo,
        ),
    )
}
