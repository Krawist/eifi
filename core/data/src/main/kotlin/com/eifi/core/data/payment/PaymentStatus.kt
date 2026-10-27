package com.eifi.core.data.payment

/** Lifecycle of a payment; only [Pending] moves on. */
enum class PaymentStatus { Pending, Confirmed, Expired, Cancelled }
