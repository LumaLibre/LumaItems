package dev.jsinco.luma.lumaitems.util.extensions

import java.awt.Color as AwtColor
import org.bukkit.Color as BukkitColor

object ColorUtil {

    fun AwtColor.toBukkitColor(): BukkitColor {
        return BukkitColor.fromRGB(this.red, this.green, this.blue)
    }

    fun BukkitColor.toAwtColor(): AwtColor {
        return AwtColor(this.red, this.green, this.blue)
    }

}