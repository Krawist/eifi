package com.eifi.feature.receive

import com.eifi.core.data.ReceivingProfileRepository
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class ReceiveEntryViewModelTest {
    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class ProfileConfigured {
        @Test
        fun `should send to the receiving profile while it is not filled in`() {
            // when
            val viewModel = ReceiveEntryViewModel(repository(configured = false))

            // then
            viewModel.profileConfigured.value?.shouldBeFalse()
        }

        @Test
        fun `should open the Receive mode once the profile is filled in`() {
            // when
            val viewModel = ReceiveEntryViewModel(repository(configured = true))

            // then
            viewModel.profileConfigured.value?.shouldBeTrue()
        }
    }

    private fun repository(configured: Boolean) = object : ReceivingProfileRepository {
        override val isConfigured = flowOf(configured)
    }
}
