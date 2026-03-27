package dev.lumas.lumaitems.util

import dev.lumas.lumaitems.model.ColorHolder
import kotlin.math.sqrt
import org.bukkit.DyeColor
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object DyeColorUtil {

    fun closestDye(holder: ColorHolder): DyeColor {
        val color = holder.getColor()
        val r = color.red
        val g = color.green
        val b = color.blue

        // Snap desaturated colors to achromatic dyes
        if (saturation(r, g, b) < 0.10) {
            val brightness = r * 0.299 + g * 0.587 + b * 0.114
            return when {
                brightness > 200 -> DyeColor.WHITE
                brightness > 130 -> DyeColor.LIGHT_GRAY
                brightness > 60 -> DyeColor.GRAY
                else -> DyeColor.BLACK
            }
        }

        return DyeColor.entries.minBy { dye ->
            val dc = dye.color
            colorDistance(r, g, b, dc.red, dc.green, dc.blue)
        }
    }

    fun dyeMaterial(holder: ColorHolder): Material {
        val dye = closestDye(holder)
        return Material.valueOf("${dye.name}_DYE")
    }

    private fun colorDistance(r1: Int, g1: Int, b1: Int, r2: Int, g2: Int, b2: Int): Double {
        val dr = r1 - r2
        val dg = g1 - g2
        val db = b1 - b2
        // Weighted for human perception
        return sqrt(2.0 * dr * dr + 4.0 * dg * dg + 3.0 * db * db)
    }

    private fun saturation(r: Int, g: Int, b: Int): Double {
        val max = maxOf(r, g, b).toDouble()
        val min = minOf(r, g, b).toDouble()
        return if (max == 0.0) 0.0 else (max - min) / max
    }
}