package io.github.anders81fin.nullplate.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class ProgressTest {
    @Test
    fun `progress caps at one`() {
        assertEquals(1.0, progressFraction(20.0, 16.0), 0.0)
        assertEquals(0.5, progressFraction(8.0, 16.0), 0.0)
    }

    @Test
    fun `a zero target reports no progress`() {
        assertEquals(0.0, progressFraction(5.0, 0.0), 0.0)
    }

    @Test
    fun `elapsed never goes negative`() {
        assertEquals(0.0, elapsedHours(100, 50), 0.0)
    }

    @Test
    fun `next boundary is the upcoming whole hour of the fast`() {
        val start = 1_000_000L
        assertEquals(start + 3600, nextHourBoundaryEpochSeconds(start, start))
        assertEquals(start + 3600, nextHourBoundaryEpochSeconds(start, start + 3599))
        assertEquals(start + 7200, nextHourBoundaryEpochSeconds(start, start + 3600))
    }

    @Test
    fun `a boundary far into the fast is still the next one, not a replay`() {
        val start = 1_000_000L
        assertEquals(start + 10 * 3600, nextHourBoundaryEpochSeconds(start, start + 9 * 3600 + 5))
    }
}
