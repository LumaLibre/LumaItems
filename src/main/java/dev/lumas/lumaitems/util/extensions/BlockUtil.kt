package dev.lumas.lumaitems.util.extensions

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.enums.BlockConstants
import dev.lumas.lumaitems.util.Util
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

// TODO: Migrate block utilities here
object BlockUtil {

    val BLOCK_FACE_RELATIVES = listOf(BlockFace.UP, BlockFace.DOWN, BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)

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

    fun Block.breakNaturallyWithLog(player: Player, itemStack: ItemStack? = null, triggerEffects: Boolean = false, dropExp: Boolean = false) {
        LumaItems.getCoreProtectAPI()?.logRemoval(player.name, this.location, this.type, this.blockData)
        itemStack?.let { this.breakNaturally(it, triggerEffects, dropExp) } ?: this.breakNaturally()
    }
    fun Block.breakNaturallyWithLog(player: Player, triggerEffects: Boolean, dropExp: Boolean) {
        LumaItems.getCoreProtectAPI()?.logRemoval(player.name, this.location, this.type, this.blockData)
        this.breakNaturally(triggerEffects, dropExp)
    }
    fun Block.setAirWithLog(player: Player) {
        LumaItems.getCoreProtectAPI()?.logRemoval(player.name, this.location, this.type, this.blockData)
        this.type = Material.AIR
    }

    fun Block.setBlockDataWithLog(player: Player, material: Material) {
        LumaItems.getCoreProtectAPI()?.logPlacement(player.name, this.location, material, material.createBlockData())
        this.blockData = material.createBlockData()
    }

    fun Block.determineHighestBreakSpeed(vararg itemStacks: ItemStack): ItemStack {
        var bestTool: ItemStack = itemStacks.first()
        var highestSpeed = 1.0F

        for (i in 1 until itemStacks.size) {
            val toolSpeed = this.getDestroySpeed(itemStacks[i])
            if (toolSpeed > highestSpeed) {
                highestSpeed = toolSpeed
                bestTool = itemStacks[i]
            }
        }

        return bestTool
    }


    fun Block.relatives(consumer: (Block) -> Unit) {
        for (face in BLOCK_FACE_RELATIVES) {
            consumer(this.getRelative(face))
        }
    }

    fun Block.relatives(vararg exclude: BlockFace): List<Block> {
        val relatives = mutableListOf<Block>()
        for (face in BLOCK_FACE_RELATIVES.filter { !exclude.contains(it) }) {
            relatives.add(this.getRelative(face))
        }
        return relatives
    }
}