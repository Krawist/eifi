package com.eifi.core.payload

/** Mobile money operators. [wireCode] identifies the operator in the payment URL and must never change. */
enum class Operator(internal val wireCode: Int) {
    OrangeMoney(wireCode = 1),
    MtnMomo(wireCode = 2),
}
