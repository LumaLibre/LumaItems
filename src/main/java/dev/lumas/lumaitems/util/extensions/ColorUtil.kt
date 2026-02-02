@file:JvmName("ColorUtil")
package dev.lumas.lumaitems.util.extensions

import java.awt.Color
import org.bukkit.Color as BukkitColor

fun Color.toBukkitColor(): BukkitColor {
    return BukkitColor.fromRGB(this.red, this.green, this.blue)
}

fun BukkitColor.toColor(): Color {
    return Color(this.red, this.green, this.blue)
}

fun String.toColor(): Color {
    return Color.decode(this)
}

fun String.toBukkitColor(): BukkitColor {
    return this.toColor().toBukkitColor()
}

fun blend(vararg c: Color): Color {
    val ratio = 1f / (c.size.toFloat())

    var a = 0
    var r = 0
    var g = 0
    var b = 0

    for (i in c.indices) {
        val rgb: Int = c[i].rgb
        val a1 = (rgb shr 24 and 0xff)
        val r1 = ((rgb and 0xff0000) shr 16)
        val g1 = ((rgb and 0xff00) shr 8)
        val b1 = (rgb and 0xff)
        a = (a + (a1 * ratio)).toInt()
        r = (r + (r1 * ratio)).toInt()
        g = (g + (g1 * ratio)).toInt()
        b = (b + (b1 * ratio)).toInt()
    }

    return Color(a shl 24 or (r shl 16) or (g shl 8) or b)
}