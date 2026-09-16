package io.github.anders81fin.nullplate.data

import io.github.anders81fin.nullplate.domain.FastingState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class IdleStateTest {

    private fun status(fasting: Boolean = false, idle: Boolean = false, lastEnd: Long = 0) =
        FastingStatus(
            state = FastingState(fasting = fasting, startedAt = if (fasting) 1_000 else 0, idle = idle),
            lastEnd = lastEnd,
        )

    @Test
    fun `a fresh install is not counting anything`() {
        assertFalse(status().counting)
        assertFalse(status().eatingWindowOpen)
    }

    @Test
    fun `a finished fast opens the eating window`() {
        val s = status(lastEnd = 1_789_000_000)
        assertTrue(s.eatingWindowOpen)
        assertTrue(s.counting)
    }

    @Test
    fun `stopping closes the eating window even though the history remains`() {
        val s = status(idle = true, lastEnd = 1_789_000_000)
        assertFalse("a stopped clock must not keep counting", s.eatingWindowOpen)
        assertFalse(s.counting)
    }

    @Test
    fun `a running fast counts regardless of the idle flag`() {
        // start() clears idle, so this pairing should never reach disk -- but the
        // fast is the stronger signal and must win if it somehow does.
        assertTrue(status(fasting = true, idle = true).counting)
    }

    @Test
    fun `a fast is never mistaken for an eating window`() {
        assertFalse(status(fasting = true, lastEnd = 1_789_000_000).eatingWindowOpen)
    }

    @Test
    fun `a state file written before idle existed keeps its eating window`() = runTest {
        // The upgrade path: users mid-eating-window when they install this
        // version must not have the clock silently stop under them.
        val legacy = """{"fasting":false,"startedAt":0,"targetHours":16.0}"""
        val restored = FastingStateSerializer.readFrom(ByteArrayInputStream(legacy.toByteArray()))

        assertFalse(restored.idle)
        assertTrue(FastingStatus(state = restored, lastEnd = 1_789_000_000).eatingWindowOpen)
    }

    @Test
    fun `the idle flag survives a write and read round trip`() = runTest {
        val original = FastingState(fasting = false, startedAt = 0, targetHours = 18.0, idle = true)
        val out = java.io.ByteArrayOutputStream()
        FastingStateSerializer.writeTo(original, out)
        val restored = FastingStateSerializer.readFrom(
            ByteArrayInputStream(out.toByteArray()),
        )
        assertEquals(original, restored)
        assertTrue("idle must be persisted, not recomputed", restored.idle)
    }
}
