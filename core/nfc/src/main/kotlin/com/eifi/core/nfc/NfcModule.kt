package com.eifi.core.nfc

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** NFC bindings. Still to come: the real reader on the Pay side (EIFI-200). */
@Module
@InstallIn(SingletonComponent::class)
internal abstract class NfcModule {
    @Binds
    abstract fun bindNfcStatusProvider(impl: AndroidNfcStatusProvider): NfcStatusProvider

    @Binds
    abstract fun bindNfcPaymentEmitter(impl: HceNfcPaymentEmitter): NfcPaymentEmitter

    @Binds
    abstract fun bindNfcPaymentReader(impl: FakeNfcPaymentReader): NfcPaymentReader
}
