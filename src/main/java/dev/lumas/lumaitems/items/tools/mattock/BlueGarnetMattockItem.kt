package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.BlockConstants
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.shapes.ShapeUtil
import dev.lumas.lumaitems.util.extensions.BlockUtil.breakNaturallyWithLog
import dev.lumas.lumaitems.util.disabling.Disable
import dev.lumas.lumaitems.util.disabling.WorldName
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class BlueGarnetMattockItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#3A7FEA&lB&#3790E4&ll&#34A0DD&lu&#31B1D7&le &#2BD2CA&lG&#28E2C4&la&#49CABA&lr&#6AB1B0&ln&#8B99A7&le&#AB819D&lt &#ED5089&lM&#EC4C7F&la&#EB4875&lt&#EA456C&lt&#E94162&lo&#E83D58&lc&#E7394E&lk",
            mutableListOf("&#6AB1B0Destructive"),
            mutableListOf("Breaks blocks in a 3x3 radius."),
            Material.NETHERITE_PICKAXE,
            mutableListOf("bluegarnetmattock"),
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.UNBREAKING to 10, Enchantment.SILK_TOUCH to 1, Enchantment.MENDING to 1)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        return Pair("bluegarnetmattock", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.BREAK_BLOCK -> {
                event as BlockBreakEvent
                val b = event.block

                val blocklist = ShapeUtil.getCuboidBlocks(b.location.add(1.0, 1.0, 1.0), b.location.add(-1.0, -1.0, -1.0)).filter {
                    !BlockConstants.BLACKLISTED.contains(it.type) && it.isSolid
                }


                for (block in blocklist) {
                    block.world.spawnParticle(Particle.BLOCK, block.location.add(0.5, 0.5, 0.5), 10, 0.5, 0.5, 0.5, 0.1, block.blockData)
                    block.breakNaturallyWithLog(player, player.inventory.itemInMainHand)
                }
            }
            else -> return false
        }
        return true
    }

}