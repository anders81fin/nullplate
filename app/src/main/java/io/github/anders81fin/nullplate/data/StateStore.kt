package io.github.anders81fin.nullplate.data

import android.content.Context
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import io.github.anders81fin.nullplate.domain.FastingState
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

internal object FastingStateSerializer : Serializer<FastingState> {
    // Defaults are written explicitly: a target that happens to equal the
    // current default is still the user's pick, and must not follow the
    // default if that constant ever changes.
    private val json = Json { encodeDefaults = true }

    override val defaultValue = FastingState()

    // Unreadable state falls back to defaults rather than surfacing an error,
    // matching what the Python backend did with a corrupt state.json.
    override suspend fun readFrom(input: InputStream): FastingState = try {
        json.decodeFromString(FastingState.serializer(), input.readBytes().decodeToString())
    } catch (_: SerializationException) {
        defaultValue
    }

    override suspend fun writeTo(t: FastingState, output: OutputStream) {
        output.write(json.encodeToString(FastingState.serializer(), t).encodeToByteArray())
    }
}

// A delegate, not a constructor call: DataStore throws if two instances are
// open on one file in the same process, and the worker resolves this from a
// different entry point than the UI does.
internal val Context.stateDataStore by dataStore("fasting_state.json", FastingStateSerializer)
