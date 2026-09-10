package io.github.anders81fin.nullplate.data

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import io.github.anders81fin.nullplate.domain.FastEntry
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object HistorySerializer : Serializer<List<FastEntry>> {
    private val serializer = ListSerializer(FastEntry.serializer())
    private val json = Json { encodeDefaults = true }

    override val defaultValue = emptyList<FastEntry>()

    override suspend fun readFrom(input: InputStream): List<FastEntry> = try {
        json.decodeFromString(serializer, input.readBytes().decodeToString())
    } catch (_: SerializationException) {
        defaultValue
    }

    override suspend fun writeTo(t: List<FastEntry>, output: OutputStream) {
        output.write(json.encodeToString(serializer, t).encodeToByteArray())
    }
}

internal val Context.historyDataStore by dataStore("fasting_history.json", HistorySerializer)
