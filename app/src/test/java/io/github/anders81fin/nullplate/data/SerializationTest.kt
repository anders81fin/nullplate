package io.github.anders81fin.nullplate.data

import io.github.anders81fin.nullplate.domain.FastEntry
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
    fun `a backup survives a round trip`() {
        val original = Backup(
            exportedAt = 1_789_040_464,
            state = FastingState(fasting = true, startedAt = 1_789_000_000, targetHours = 18.0),
            history = listOf(
                FastEntry(start = 1, end = 2, targetHours = 16.0, actualHours = 16.5),
                FastEntry(start = 3, end = 4, targetHours = 20.0, actualHours = 19.0),
            ),
        )
        assertEquals(original, decodeBackup(encodeBackup(original)))
    }

    @Test
    fun `an exported backup stays readable by hand`() {
        val text = encodeBackup(
            Backup(exportedAt = 1, state = FastingState(), history = emptyList()),
        )
        assertTrue("expected pretty-printed JSON, got: $text", text.contains("\n"))
    }

    @Test
    fun `unreadable state falls back to defaults instead of throwing`() = runTest {
        val restored = FastingStateSerializer.readFrom(ByteArrayInputStream("not json".toByteArray()))
        assertEquals(FastingState(), restored)
    }
}
