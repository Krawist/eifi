package com.eifi.core.nfc

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class NdefUriMessageTest {
    @Nested
    inner class NdefUriMessage {
        @Test
        fun `should write a short URI record with https abbreviated`() {
            // when
            val message = ndefUriMessage("https://pay.eifi.app/p#v1.a")

            // then
            message.toList() shouldBe (
                byteArrayOf(0xD1.toByte(), 0x01, 20, 'U'.code.toByte(), 0x04) +
                    "pay.eifi.app/p#v1.a".encodeToByteArray()
                ).toList()
        }

        @Test
        fun `should write a long record beyond 255 bytes of payload`() {
            // given
            val url = "https://pay.eifi.app/p#v1." + "a".repeat(300)

            // when
            val message = ndefUriMessage(url)

            // then
            message.copyOfRange(0, 7).toList() shouldBe
                byteArrayOf(0xC1.toByte(), 0x01, 0, 0, 0x01, 0x3F, 'U'.code.toByte()).toList()
        }
    }
}
