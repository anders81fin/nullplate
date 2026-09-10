package io.github.anders81fin.nullplate.domain

data class Stage(val title: String, val blurb: String)

// Tongue-in-cheek physiology timeline. Hour thresholds are the usual rough IF
// folklore, not medical advice.
fun fastingStage(hours: Double): Stage = when {
    hours < 4 -> Stage("Fresh from the table", "blood sugar's still up from that last meal")
    hours < 8 -> Stage("Glycogen cruise control", "burning through the liver's sugar stash")
    hours < 12 -> Stage("Tank running low", "glycogen reserves are thinning out")
    hours < 18 -> Stage("Metabolic switch flipping", "body's starting to eye the fat reserves")
    hours < 24 -> Stage("Ketosis kicking in", "fat's becoming the fuel of choice")
    hours < 36 -> Stage("Deep ketosis", "you're basically a furnace now")
    hours < 48 -> Stage("Autophagy o'clock", "cells are grabbing their brooms")
    else -> Stage("Legendary autophagy", "full cellular renovation crew, hard hats on")
}

fun eatingStage(hoursElapsed: Double, targetHours: Double): Stage {
    if (targetHours <= 0) {
        return Stage("Zero eating window", "UMAD picked the nuclear option, respect")
    }
    val remaining = targetHours - hoursElapsed
    return when {
        remaining > targetHours * 0.5 -> Stage("Fueling up", "eat well, the fast comes back around")
        remaining > 0 -> Stage("Window closing soon", "last orders, plan that final bite")
        else -> Stage("Into overtime", "window's technically closed, no judgment")
    }
}
