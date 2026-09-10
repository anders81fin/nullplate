package io.github.anders81fin.nullplate.data

import io.github.anders81fin.nullplate.domain.FastEntry
import io.github.anders81fin.nullplate.domain.FastingState
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

const val BACKUP_VERSION = 1

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

fun decodeBackup(text: String): Backup =
    backupJson.decodeFromString(Backup.serializer(), text)
