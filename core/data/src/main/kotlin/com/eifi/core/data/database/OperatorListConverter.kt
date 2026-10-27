package com.eifi.core.data.database

import androidx.room.TypeConverter
import com.eifi.core.payload.Operator

/** Stores a list of operators as their names separated by commas, e.g. `OrangeMoney,MtnMomo`. */
internal class OperatorListConverter {
    @TypeConverter
    fun fromOperators(operators: List<Operator>?): String? = operators?.joinToString(separator = SEPARATOR) { it.name }

    @TypeConverter
    fun toOperators(value: String?): List<Operator>? =
        value?.split(SEPARATOR)?.filter { it.isNotEmpty() }?.map(Operator::valueOf)

    private companion object {
        const val SEPARATOR = ","
    }
}
