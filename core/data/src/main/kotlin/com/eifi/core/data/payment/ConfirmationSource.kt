package com.eifi.core.data.payment

/** What confirmed a payment: the operator's credit SMS, or the payee after checking it themselves. */
enum class ConfirmationSource { Sms, Manual }
