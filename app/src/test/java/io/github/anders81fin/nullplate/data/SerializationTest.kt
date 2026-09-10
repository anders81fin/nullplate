package io.github.anders81fin.nullplate.data

import io.github.anders81fin.nullplate.domain.FastingState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SerializationTest {

    private suspend fun persisted(state: FastingState): String {
        val out = ByteArrayOutputStream()
        FastingStateSerializer.writeTo(state, out)
        return out.toString(Charsets.UTF_8.name())
    }

    @Test
    fun `a target equal to the default is still written to disk`() = runTest {
        val json = persisted(FastingState(fasting = true, startedAt = 1_000, targetHours = 16.0))
        assertTrue("targetHours missing from $json", json.contains("targetHours"))
    }

    @Test
    fun `state survives a write and read round trip`() = runTest {
        val original = FastingState(fasting = true, startedAt = 1_789_040_464, targetHours = 20.0)
        val restored = FastingStateSerializer.readFrom(
            ByteArrayInputStream(persisted(original).toByteArray()),
        )
        assertEquals(original, restored)
    }

    @Test
    fun `unreadable state falls back to defaults instead of throwing`() = runTest {
        val restored = FastingStateSerializer.readFrom(ByteArrayInputStream("not json".toByteArray()))
        assertEquals(FastingState(), restored)
    }
}
