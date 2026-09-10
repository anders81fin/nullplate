package io.github.anders81fin.nullplate.domain

import kotlin.math.floor
import kotlin.math.roundToInt

fun formatHm(hours: Double): String {
    val totalMinutes = (hours * 60).roundToInt().coerceAtLeast(0)
    return "%d:%02d".format(totalMinutes / 60, totalMinutes % 60)
}

// QML printed whole target hours without a decimal point ("16h", not "16.0h");
// keep that so ported strings read identically.
fun formatHours(hours: Double): String =
    if (hours == floor(hours)) hours.toInt().toString() else hours.toString()
