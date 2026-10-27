package com.eifi.core.design.component

import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class EifiQrCodeTest {
    @Nested
    inner class EncodeQrCode {
        @Test
        fun `should use error correction Q`() {
            // when
            val qrCode = encodeQrCode("https://pay.eifi.app/p#v1.abc")

            // then
            qrCode.ecLevel shouldBe ErrorCorrectionLevel.Q
        }

        @Test
        fun `should fit a typical payment URL in version 8`() {
            // given: 90 bytes, the size of a typical URL measured in EIFI-020
            val url = "https://pay.eifi.app/p#v1." + "A".repeat(64)

            // when
            val qrCode = encodeQrCode(url)

            // then
            qrCode.version.versionNumber shouldBeLessThanOrEqual 8
        }
    }
}
