package com.eifi.core.nfc

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** NFC bindings. */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NfcModule {
    @Binds
    abstract fun bindNfcStatusProvider(impl: AndroidNfcStatusProvider): NfcStatusProvider

    @Binds
    abstract fun bindNfcPaymentEmitter(impl: HceNfcPaymentEmitter): NfcPaymentEmitter

    @Binds
    abstract fun bindNfcPaymentReader(impl: AndroidNfcPaymentReader): NfcPaymentReader
}
