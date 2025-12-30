package dev.lumas.lumaitems.util.extensions

import java.awt.Color
import org.bukkit.Color as BukkitColor

object ColorUtil {

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

}