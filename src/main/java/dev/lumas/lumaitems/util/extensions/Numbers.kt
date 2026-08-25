@file:JvmName("Numbers")
package dev.lumas.lumaitems.util.extensions

import java.util.function.IntFunction
import java.util.stream.IntStream

fun String.toIntOrZero(): Int {
    return this.toIntOrNull() ?: 0
}

fun String.toIntOr(default: Int): Int {
    return this.toIntOrNull() ?: default
}

fun Long.ticksAsFormattedTime(): String {
    val seconds = (this + 19L) / 20L
    val hours = seconds / 3600L
    val minutes = (seconds % 3600L) / 60L
    val rest = seconds % 60L

    return buildString {
        if (hours > 0) append("${hours}h ")
        if (hours > 0 || minutes > 0) append("${minutes}m")
        if (rest > 0 || isEmpty()) {
            if (isNotEmpty()) append(" ")
            append("${rest}s")
        }
    }
}

fun String.getNextIntArgument(): List<String> {
    val num: Int = this.toIntOr(-1)
    return IntStream.range(0, 10)
        .mapToObj(IntFunction { i: Int -> if (num < 0) i.toString() else num.toString() + "" + i })
        .toList()
}