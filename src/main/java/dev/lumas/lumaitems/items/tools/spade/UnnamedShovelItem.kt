package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class UnnamedShovelItem : CustomItemFunctions() {

    private companion object {
        const val MAX_WALK_HEIGHT = 2
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Unnamed Shovel 100")
            .customEnchants("unnamed")
            .material(Material.NETHERITE_SHOVEL)
            .persistentData("unnamed-shovel-100")
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        val item = event.player.inventory.itemInMainHand

        if (!block.type.hasGravity() || !block.blockData.isPreferredTool(item)) {
            return
        }

        // walk the blocks upwards to find all connected blocks of the same type
        for (y in block.y + 1..block.y + MAX_WALK_HEIGHT) {
            val currentBlock = block.world.getBlockAt(block.x, y, block.z)
            if (currentBlock.type != block.type) {
                break
            }

            currentBlock.breakNaturallyWithLog(player, item, true)
        }
    }
}