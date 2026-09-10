package io.github.anders81fin.nullplate.domain

data class Preset(val hours: Double, val label: String)

// All but the joke entry sum to 24h, so the eating-window target is simply
// derived as 24 - targetHours.
val PRESETS = listOf(
    Preset(14.0, "14:10"),
    Preset(16.0, "16:8"),
    Preset(18.0, "18:6"),
    Preset(20.0, "20:4"),
    Preset(22.0, "22:2"),
    Preset(24.0, "UMAD?"),
)

const val DEFAULT_TARGET_HOURS = 16.0

fun eatingTargetHours(targetHours: Double): Double = (24.0 - targetHours).coerceAtLeast(0.0)
