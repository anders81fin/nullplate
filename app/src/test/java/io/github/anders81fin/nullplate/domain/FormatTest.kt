package io.github.anders81fin.nullplate.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {
    @Test
    fun `minutes are zero padded`() {
        assertEquals("0:00", formatHm(0.0))
        assertEquals("1:30", formatHm(1.5))
        assertEquals("16:00", formatHm(16.0))
        assertEquals("0:05", formatHm(5.0 / 60))
    }

    @Test
    fun `negative elapsed clamps to zero`() {
        assertEquals("0:00", formatHm(-3.0))
    }

    @Test
    fun `whole target hours print without a decimal`() {
        assertEquals("16", formatHours(16.0))
        assertEquals("24", formatHours(24.0))
        assertEquals("13.5", formatHours(13.5))
    }
}
