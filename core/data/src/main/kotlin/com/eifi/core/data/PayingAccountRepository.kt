package com.eifi.core.data

import com.eifi.core.payload.Operator
import kotlinx.coroutines.flow.Flow

/** The payer's account used for the last payment, offered first on the next summary (EIFI-201). */
interface PayingAccountRepository {
    /** `null` until the first payment. */
    val lastOperator: Flow<Operator?>

    suspend fun saveLastOperator(operator: Operator)
}
