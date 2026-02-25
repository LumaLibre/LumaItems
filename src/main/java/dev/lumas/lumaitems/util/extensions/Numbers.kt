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

fun String.getNextIntArgument(): List<String> {
    val num: Int = this.toIntOr(-1)
    return IntStream.range(0, 10)
        .mapToObj(IntFunction { i: Int -> if (num < 0) i.toString() else num.toString() + "" + i })
        .toList()
}