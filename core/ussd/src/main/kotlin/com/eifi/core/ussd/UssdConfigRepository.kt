package com.eifi.core.ussd

import kotlinx.coroutines.flow.Flow

/** The USSD codes of the supported operators, stored locally (`RoomUssdConfigRepository` in `:core:data`). */
interface UssdConfigRepository {
    /** One config per supported operator. */
    val configs: Flow<List<OperatorUssdConfig>>
}
