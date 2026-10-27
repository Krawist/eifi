package com.eifi.core.data

import kotlinx.coroutines.flow.Flow

/** The payer's remembered account, offered first on the next summaries (EIFI-201, EIFI-202). */
interface PayingAccountRepository {
    /** `null` until an account is remembered. */
    val choice: Flow<PayingAccountChoice?>

    suspend fun saveChoice(choice: PayingAccountChoice)
}
