package com.eifi.core.data

import com.eifi.core.payload.Operator
import com.eifi.core.payload.ReceivingMethod
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class ReceivingProfileTest {
    @Nested
    inner class Init {
        @ParameterizedTest
        @ValueSource(strings = [" ", "1234567890123456789012345678901"])
        fun `should reject a blank display name or one longer than 30 characters`(name: String) {
            // when & then
            shouldThrow<IllegalArgumentException> {
                ReceivingProfile(
                    displayName = name,
                    receivingMethods = mapOf(Operator.OrangeMoney to ReceivingMethod.MerchantCode("482913")),
                )
            } shouldHaveMessage "The display name must have 1 to 30 characters"
        }

        @Test
        fun `should require at least one operator`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                ReceivingProfile(displayName = "Kiosque", receivingMethods = emptyMap())
            } shouldHaveMessage "At least one operator is required"
        }
    }
}
