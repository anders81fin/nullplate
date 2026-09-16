package io.github.anders81fin.nullplate.data

import io.github.anders81fin.nullplate.domain.FastEntry
import io.github.anders81fin.nullplate.domain.FastingState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream

class BackupImportTest {

    private fun backupText(version: Int = BACKUP_VERSION, history: List<FastEntry>) =
        encodeBackup(
            Backup(
                version = version,
                exportedAt = 1_789_040_464,
                state = FastingState(),
                history = history,
            ),
        )

    private val good = FastEntry(start = 100, end = 3_700, targetHours = 16.0, actualHours = 1.0)

    // --- bounded read ------------------------------------------------------

    @Test
    fun `a stream within the cap is read whole`() {
        val text = "x".repeat(1000)
        assertEquals(text, ByteArrayInputStream(text.toByteArray()).readAtMost(MAX_BACKUP_BYTES))
    }

    @Test
    fun `a stream over the cap is refused rather than read`() {
        // The picker hands over whatever the user tapped; a video must not be
        // pulled into memory before we notice it is not a backup.
        val huge = ByteArray(MAX_BACKUP_BYTES + 1024)
        val e = assertThrows(BackupFormatException::class.java) {
            ByteArrayInputStream(huge).readAtMost(MAX_BACKUP_BYTES)
        }
        assertTrue("expected a size message, got: ${e.message}", e.message!!.contains("larger"))
    }

    @Test
    fun `the cap is exact at the boundary`() {
        val atLimit = ByteArray(MAX_BACKUP_BYTES) { 'a'.code.toByte() }
        assertEquals(
            MAX_BACKUP_BYTES,
            ByteArrayInputStream(atLimit).readAtMost(MAX_BACKUP_BYTES).length,
        )
    }

    // --- version gate ------------------------------------------------------

    @Test
    fun `a backup from this version is accepted`() {
        assertEquals(1, decodeBackup(backupText(history = listOf(good))).history.size)
    }

    @Test
    fun `a backup from a newer format is refused`() {
        // BACKUP_VERSION existed but was never checked, so a future format would
        // have been accepted and silently mis-read.
        val e = assertThrows(BackupFormatException::class.java) {
            decodeBackup(backupText(version = BACKUP_VERSION + 1, history = listOf(good)))
        }
        assertTrue("expected a version message, got: ${e.message}", e.message!!.contains("version"))
    }

    // --- entry validation --------------------------------------------------

    @Test
    fun `an entry that ends before it starts is dropped`() {
        val backwards = FastEntry(start = 5_000, end = 100, targetHours = 16.0, actualHours = 1.0)
        val restored = decodeBackup(backupText(history = listOf(good, backwards)))
        assertEquals(listOf(good), restored.history)
    }

    @Test
    fun `entries with impossible numbers are dropped`() {
        val negativeHours = good.copy(actualHours = -5.0)
        val zeroTarget = good.copy(targetHours = 0.0)
        val zeroStart = good.copy(start = 0)
        val restored = decodeBackup(
            backupText(history = listOf(negativeHours, good, zeroTarget, zeroStart)),
        )
        assertEquals(listOf(good), restored.history)
    }

    @Test
    fun `a backup whose entries are all junk imports as empty rather than failing`() {
        // Dropping entries must not become a hard failure: the rest of the file
        // is still worth restoring.
        val junk = FastEntry(start = -1, end = -1, targetHours = -1.0, actualHours = -1.0)
        assertEquals(emptyList<FastEntry>(), decodeBackup(backupText(history = listOf(junk))).history)
    }

    @Test
    fun `a round trip of good history is unchanged`() {
        val history = listOf(good, good.copy(start = 10_000, end = 20_000))
        assertEquals(history, decodeBackup(backupText(history = history)).history)
    }
}
