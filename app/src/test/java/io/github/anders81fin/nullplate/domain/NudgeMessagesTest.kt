package io.github.anders81fin.nullplate.domain

import org.junit.Assert.assertTrue
import org.junit.Test

class NudgeMessagesTest {

    @Test
    fun `the first hour is singular`() {
        val messages = fastHourMessages(1, 16.0)
        assertTrue(messages.any { it.startsWith("1 hour in.") })
        assertTrue(messages.none { it.contains("1 hours") })
    }

    @Test
    fun `later hours stay plural`() {
        assertTrue(fastHourMessages(5, 16.0).any { it.startsWith("5 hours in.") })
    }

    @Test
    fun `the countdown subtracts the hours already fasted`() {
        assertTrue(fastHourMessages(1, 14.0).any { it == "1h down, 13h to go. Onward." })
        assertTrue(fastHourMessages(9, 14.0).any { it == "9h down, 5h to go. Onward." })
    }

    @Test
    fun `past the target it stops counting down instead of going negative`() {
        val messages = fastHourMessages(15, 14.0)
        assertTrue(messages.none { it.contains("to go") })
        assertTrue(messages.any { it.contains("past the target") })
    }

    @Test
    fun `reaching the target exactly is already past it`() {
        assertTrue(fastHourMessages(14, 14.0).none { it.contains("to go") })
    }
}
