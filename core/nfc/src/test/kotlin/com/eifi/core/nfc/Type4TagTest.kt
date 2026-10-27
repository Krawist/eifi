package com.eifi.core.nfc

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

internal class Type4TagTest {
    @Nested
    inner class Process {
        @Test
        fun `should give the NDEF message to a reader following the Type 4 sequence`() {
            // given
            val message = ndefUriMessage("https://pay.eifi.app/p#v1.a")
            val tag = Type4Tag(message)

            // when
            tag.process(SELECT_APPLICATION)
            tag.process(SELECT_CAPABILITY_CONTAINER)
            tag.process(hex("00B000000F"))
            tag.process(SELECT_NDEF_FILE)
            val length = tag.process(hex("00B0000002"))
            val content = tag.process(hex("00B00002") + message.size.toByte())

            // then
            length.toList() shouldBe (byteArrayOf(0, message.size.toByte()) + OK).toList()
            content.toList() shouldBe (message + OK).toList()
        }

        @Test
        fun `should describe a read-only NDEF file in the capability container`() {
            // given
            val tag = Type4Tag(ndefUriMessage("https://pay.eifi.app/p#v1.a"))
            tag.process(SELECT_APPLICATION)
            tag.process(SELECT_CAPABILITY_CONTAINER)

            // when
            val response = tag.process(hex("00B000000F"))

            // then
            response.toList() shouldBe hex("000F2000FF00FF0406E104040000FF9000").toList()
        }

        @Test
        fun `should refuse to select a file before the NDEF application`() {
            // given
            val tag = Type4Tag(ndefUriMessage("https://pay.eifi.app/p#v1.a"))

            // when
            val response = tag.process(SELECT_NDEF_FILE)

            // then
            response.toList() shouldBe Type4Tag.FILE_NOT_FOUND.toList()
        }

        @Test
        fun `should refuse to read before a file is selected`() {
            // given
            val tag = Type4Tag(ndefUriMessage("https://pay.eifi.app/p#v1.a"))
            tag.process(SELECT_APPLICATION)

            // when
            val response = tag.process(hex("00B0000002"))

            // then
            response.toList() shouldBe Type4Tag.FILE_NOT_FOUND.toList()
        }

        @Test
        fun `should refuse an unknown instruction`() {
            // given
            val tag = Type4Tag(ndefUriMessage("https://pay.eifi.app/p#v1.a"))
            tag.process(SELECT_APPLICATION)

            // when
            val response = tag.process(hex("00D6000001AA"))

            // then
            response.toList() shouldBe hex("6D00").toList()
        }
    }

    @Nested
    inner class Reset {
        @Test
        fun `should require the application to be selected again`() {
            // given
            val tag = Type4Tag(ndefUriMessage("https://pay.eifi.app/p#v1.a"))
            tag.process(SELECT_APPLICATION)

            // when
            tag.reset()

            // then
            tag.process(SELECT_NDEF_FILE).toList() shouldBe Type4Tag.FILE_NOT_FOUND.toList()
        }
    }

    private companion object {
        val SELECT_APPLICATION = hex("00A4040007D276000085010100")
        val SELECT_CAPABILITY_CONTAINER = hex("00A4000C02E103")
        val SELECT_NDEF_FILE = hex("00A4000C02E104")
        val OK = hex("9000")

        fun hex(value: String): ByteArray = value.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
    }
}
