package com.eifi.core.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eifi.core.payload.Operator
import com.eifi.core.ussd.OperatorUssdConfig

/**
 * Row of [OperatorUssdConfig]; each column holds the property of the same name.
 *
 * @property operator primary key, [OperatorUssdConfig.operator].
 * @property merchantPayment [OperatorUssdConfig.merchantPayment].
 * @property transfer [OperatorUssdConfig.transfer].
 */
@Entity(tableName = "ussd_config")
internal data class UssdConfigEntity(
    @PrimaryKey val operator: Operator,
    val merchantPayment: String?,
    val transfer: String?,
) {
    fun toConfig() = OperatorUssdConfig(operator = operator, merchantPayment = merchantPayment, transfer = transfer)

    companion object {
        fun of(config: OperatorUssdConfig) = UssdConfigEntity(
            operator = config.operator,
            merchantPayment = config.merchantPayment,
            transfer = config.transfer,
        )
    }
}
