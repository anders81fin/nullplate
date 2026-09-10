package io.github.anders81fin.nullplate.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class FastEntryTest {
    @Test
    fun `actual hours are rounded to two decimals`() {
        val state = FastingState(fasting = true, startedAt = 0, targetHours = 16.0)
        assertEquals(1.02, completedEntry(state, 3660).actualHours, 0.0)
    }

    @Test
    fun `the entry keeps the target the fast was started with`() {
        val state = FastingState(fasting = true, startedAt = 1_000_000, targetHours = 20.0)
        val entry = completedEntry(state, 1_000_000 + 18 * 3600)
        assertEquals(20.0, entry.targetHours, 0.0)
        assertEquals(18.0, entry.actualHours, 0.0)
        assertEquals(1_000_000L, entry.start)
    }
}
