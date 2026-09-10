package io.github.anders81fin.nullplate.domain

fun fastHourMessages(hours: Int, targetHours: Double): List<String> {
    val remaining = targetHours - hours
    return listOf(
        "Hour $hours down. Willpower: still fully charged.",
        "${hours}h fasted — glycogen's quietly packing its bags.",
        "Still going at ${hours}h. Future you says thanks.",
        "$hours ${if (hours == 1) "hour" else "hours"} in. Hunger's just a suggestion at this point.",
        "Clocked ${hours}h fasting. Cells are taking notes for the cleanup crew.",
        if (remaining > 0) {
            "${hours}h down, ${formatHours(remaining)}h to go. Onward."
        } else {
            "${hours}h down, past the target and still going. Onward."
        },
    )
}

fun eatingHourMessages(hours: Int): List<String> = listOf(
    "${hours}h into the eating window — get some food in before the fast's back on.",
    "Eating window: ${hours}h used. The clock's ticking toward the next fast.",
    "${hours}h in — refuel now, the fast doesn't wait around.",
    "Still got food on the table? ${hours}h into the window already.",
    "${hours}h eating so far. Don't let the window close on an empty plate.",
)
