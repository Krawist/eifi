package com.eifi.di

import com.eifi.BuildConfig
import com.eifi.core.payload.PaymentUrlCodec
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock

/** The payment URL codec, on the domain of the payment page ([BuildConfig.PAYMENT_BASE_URL]). */
@Module
@InstallIn(SingletonComponent::class)
internal object PaymentUrlModule {
    @Provides
    fun providePaymentUrlCodec(clock: Clock): PaymentUrlCodec =
        PaymentUrlCodec(baseUrl = BuildConfig.PAYMENT_BASE_URL, clock = clock)
}
