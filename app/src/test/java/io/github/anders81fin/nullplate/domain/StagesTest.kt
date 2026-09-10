package io.github.anders81fin.nullplate.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class StagesTest {
    @Test
    fun `fasting stage flips exactly on the hour threshold`() {
        assertEquals("Fresh from the table", fastingStage(3.99).title)
        assertEquals("Glycogen cruise control", fastingStage(4.0).title)
        assertEquals("Autophagy o'clock", fastingStage(47.99).title)
        assertEquals("Legendary autophagy", fastingStage(48.0).title)
    }

    @Test
    fun `a zero eating window is the UMAD case`() {
        assertEquals("Zero eating window", eatingStage(1.0, 0.0).title)
    }

    @Test
    fun `eating window walks from fueling to overtime`() {
        assertEquals("Fueling up", eatingStage(1.0, 8.0).title)
        assertEquals("Window closing soon", eatingStage(6.0, 8.0).title)
        assertEquals("Into overtime", eatingStage(8.0, 8.0).title)
    }
}
