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

fun Color.mix(other: Color, ratio: Float): Color {
    val r = (this.red * (1 - ratio) + other.red * ratio).toInt()
    val g = (this.green * (1 - ratio) + other.green * ratio).toInt()
    val b = (this.blue * (1 - ratio) + other.blue * ratio).toInt()
    val a = (this.alpha * (1 - ratio) + other.alpha * ratio).toInt()
    return Color(r, g, b, a)
}

fun Color.mix(other: Color): Color {
    return this.mix(other, 0.5f)
}

fun BukkitColor.mix(other: BukkitColor, ratio: Float): BukkitColor {
    return this.toColor().mix(other.toColor(), ratio).toBukkitColor()
}

fun BukkitColor.mix(other: BukkitColor): BukkitColor {
    return this.mix(other, 0.5f)
}

fun Color.toHex(): String {
    return String.format("#%02x%02x%02x", this.red, this.green, this.blue)
}

fun BukkitColor.toHex(): String {
    return this.toColor().toHex()
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