package com.eifi.core.ussd

import kotlinx.coroutines.flow.Flow

/** The USSD codes of the supported operators. */
interface UssdConfigRepository {
    /** One config per supported operator. */
    val configs: Flow<List<OperatorUssdConfig>>
}
