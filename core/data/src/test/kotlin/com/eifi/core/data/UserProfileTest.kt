package com.eifi.core.data

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class UserProfileTest {
    @Nested
    inner class Init {
        @Test
        fun `should require a personal or business name`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                UserProfile(personalName = null, businessName = null)
            } shouldHaveMessage "A personal or business name is required"
        }

        @ParameterizedTest
        @ValueSource(strings = [" ", "1234567890123456789012345678901"])
        fun `should reject a blank name or a name longer than 30 characters`(name: String) {
            // when & then
            shouldThrow<IllegalArgumentException> {
                UserProfile(personalName = name, businessName = "Kiosque Ndé")
            } shouldHaveMessage "A name must have 1 to 30 characters"
        }

        @Test
        fun `should count a blank name as missing`() {
            // when & then
            shouldThrow<IllegalArgumentException> {
                UserProfile(personalName = " ", businessName = null)
            } shouldHaveMessage "A personal or business name is required"
        }
    }

    @Nested
    inner class GreetingName {
        @Test
        fun `should prefer the business name`() {
            // given
            val profile = UserProfile(personalName = "Paul", businessName = "Boutique Mama Ngono")

            // when
            val name = profile.greetingName

            // then
            name shouldBe "Boutique Mama Ngono"
        }

        @Test
        fun `should use the personal name without business name`() {
            // given
            val profile = UserProfile(personalName = "Paul", businessName = null)

            // when
            val name = profile.greetingName

            // then
            name shouldBe "Paul"
        }
    }
}
