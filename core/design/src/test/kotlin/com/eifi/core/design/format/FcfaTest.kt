package com.eifi.core.design.format

import kotlin.test.Test
import kotlin.test.assertEquals

class FcfaTest {
    @Test
    fun `small amounts have no separator`() {
        assertEquals("0", formatFcfa(0))
        assertEquals("500", formatFcfa(500))
    }

    @Test
    fun `thousands are separated by a thin space`() {
        assertEquals("2${THIN_SPACE}500", formatFcfa(2_500))
        assertEquals("2${THIN_SPACE}000${THIN_SPACE}000", formatFcfa(2_000_000))
    }

    @Test
    fun `negative amount keeps its sign`() {
        assertEquals("-1${THIN_SPACE}000", formatFcfa(-1_000))
    }
}
