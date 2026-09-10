package io.github.anders81fin.nullplate.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.anders81fin.nullplate.domain.FastEntry
import io.github.anders81fin.nullplate.domain.formatHours
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val DAY_MONTH: DateTimeFormatter = DateTimeFormatter.ofPattern("d.M.", Locale.ROOT)

@Composable
fun HistorySection(title: String, entries: List<FastEntry>, modifier: Modifier = Modifier) {
    if (entries.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        entries.forEach { entry -> HistoryRow(entry) }
    }
}

@Composable
private fun HistoryRow(entry: FastEntry) {
    val hit = entry.actualHours >= entry.targetHours
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (hit) "✓" else "·",
            style = MaterialTheme.typography.bodyMedium,
            color = if (hit) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = Instant.ofEpochSecond(entry.end).atZone(ZoneId.systemDefault()).format(DAY_MONTH),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            // Locale.ROOT keeps the decimal point a dot: the device locale would
            // render 16,5 here while every other number in the app uses a dot.
            text = "%.1fh / %sh".format(Locale.ROOT, entry.actualHours, formatHours(entry.targetHours)),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
