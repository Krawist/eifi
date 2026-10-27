package com.eifi.core.ussd.fake

import com.eifi.core.payload.Operator
import com.eifi.core.ussd.OperatorUssdConfig
import com.eifi.core.ussd.UssdConfigRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

/**
 * The USSD codes validated by Krawist on real accounts (EIFI-001), embedded until they are stored locally and
 * fetched from the backend. TODO(EIFI-001): the transfer codes.
 */
internal class FakeUssdConfigRepository @Inject constructor() : UssdConfigRepository {
    override val configs: Flow<List<OperatorUssdConfig>> = flowOf(
        listOf(
            OperatorUssdConfig(
                operator = Operator.OrangeMoney,
                merchantPayment = "#150*47*{code}*{amount}#",
                transfer = "#150*1*1*{code}*{amount}#",
            ),
            OperatorUssdConfig(
                operator = Operator.MtnMomo,
                merchantPayment = "*126*2*{code}*{amount}#",
                transfer = null,
            ),
        ),
    )
}
