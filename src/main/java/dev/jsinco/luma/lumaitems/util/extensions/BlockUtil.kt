package dev.jsinco.luma.lumaitems.util.extensions

import dev.jsinco.luma.lumaitems.enums.BlockConstants
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.Block

// TODO: Migrate block utilities here
object BlockUtil {

    fun Block.getOreColor(): Color? {
        if (!BlockConstants.ORES.contains(this.type)) {
            return null
        }
        val parts = this.type.name.split('_')
        val gem = when (parts.size) {
            3 -> parts[1]
            2 -> parts[0]
            else -> return null
        }

        val mat = Util.enumValueOfOrNull(Material::class.java, "${gem}_BLOCK") ?: return null
        return mat.createBlockData().mapColor
    }
}