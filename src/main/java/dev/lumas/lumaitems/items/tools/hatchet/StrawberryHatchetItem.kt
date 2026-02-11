package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.annotations.Ignore
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.setAirWithLog
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Ignore
class StrawberryHatchetItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("")
            .buildPair()
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (!Tag.LOGS.isTagged(block.type)) {
            return
        }

        event.isCancelled = true
        block.setAirWithLog(player)
        block.world.dropItemNaturally(block.location.toCenterLocation(), ItemStack(Material.CHARCOAL))
    }
}