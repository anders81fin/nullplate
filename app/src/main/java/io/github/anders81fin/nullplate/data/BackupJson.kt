package io.github.anders81fin.nullplate.data

import io.github.anders81fin.nullplate.domain.FastEntry
import io.github.anders81fin.nullplate.domain.FastingState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.ByteArrayOutputStream
import java.io.InputStream

const val BACKUP_VERSION = 1

// A backup is the one file this app reads that it did not write, picked by the
// user through the document picker, so its size is whatever they tapped -- a
// video, a disk image, a mistake. Reading it whole is how the import turns into
// an out-of-memory crash, so it is capped. Real backups are a few hundred KiB
// even after years of fasts.
const val MAX_BACKUP_BYTES = 4 * 1024 * 1024

class BackupFormatException(message: String) : Exception(message)

@Serializable
data class Backup(
    val version: Int = BACKUP_VERSION,
    val exportedAt: Long,
    val state: FastingState,
    val history: List<FastEntry>,
)

// Pretty-printed on purpose. The desktop widget kept its state in plain files
// precisely so it could be read and edited by hand, and app-private storage
// took that away; an exported file is where it comes back.
private val backupJson = Json {
    encodeDefaults = true
    prettyPrint = true
    ignoreUnknownKeys = true
}

fun encodeBackup(backup: Backup): String =
    backupJson.encodeToString(Backup.serializer(), backup)

/**
 * Parse an exported backup, rejecting what this version cannot honestly read.
 *
 * `BACKUP_VERSION` existed but nothing ever checked it, so a file written by a
 * later format would have been accepted and quietly mis-read -- the failure
 * mode being wrong history rather than an error. Entries are filtered too: a
 * hand-edited or truncated file can carry a fast that ends before it starts,
 * and that would go on to skew the streak and the longest-fast list forever.
 */
fun decodeBackup(text: String): Backup {
    val backup = backupJson.decodeFromString(Backup.serializer(), text)

    if (backup.version > BACKUP_VERSION) {
        throw BackupFormatException(
            "backup version ${backup.version} is newer than this app understands",
        )
    }

    return backup.copy(history = backup.history.filter { it.isPlausible() })
}

private fun FastEntry.isPlausible(): Boolean =
    start > 0 && end >= start && targetHours > 0 && actualHours >= 0

/**
 * Read the stream as UTF-8 text, refusing anything past [limit].
 *
 * Streams in a chunk at a time and fails as soon as the cap is passed, so an
 * oversized file costs one buffer rather than its own size in heap. The cap is
 * checked before each write, not after reading everything.
 */
fun InputStream.readAtMost(limit: Int): String {
    val out = ByteArrayOutputStream()
    val chunk = ByteArray(64 * 1024)
    while (true) {
        val read = read(chunk)
        if (read < 0) break
        if (out.size() + read > limit) {
            throw BackupFormatException("that file is larger than ${limit / (1024 * 1024)} MB")
        }
        out.write(chunk, 0, read)
    }
    return out.toString(Charsets.UTF_8.name())
}
