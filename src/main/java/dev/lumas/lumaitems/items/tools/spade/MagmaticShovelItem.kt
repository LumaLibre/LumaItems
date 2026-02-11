package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import java.util.function.Consumer
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class MagmaticShovelItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#f52a2a&lV&#f33c32&lo&#f24e39&ll&#f06041&lc&#ef7248&la&#ed8450&ln&#ec9657&li&#e99455&lc &#e47d49&lS&#df663c&lh&#db4f30&lo&#d63924&lv&#d22217&le&#cd0b0b&ll",
            mutableListOf("&#f1892aH&#f27d2ae&#f3722aa&#f4662at&#f55a2bi&#f54e2bn&#f6432bg &#f7372bU&#f82b2bp"),
            mutableListOf("Converts mined sand into glass"),
            Material.NETHERITE_SHOVEL,
            mutableListOf("magmaticshovel"),
            mutableMapOf(
                Enchantment.EFFICIENCY to 8,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.FIRE_ASPECT to 4
            )
        )
        return Pair("magmaticshovel", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val blockBreakEvent: BlockBreakEvent? = event as? BlockBreakEvent

        when (type) {
            Action.BREAK_BLOCK -> {
                heatingUp(blockBreakEvent!!.block, blockBreakEvent.block.getDrops(player.inventory.itemInMainHand))
            }
            else -> return false
        }
        return true
    }

    private fun heatingUp(blockBroken: Block, drops: Collection<ItemStack>) {
        if (blockBroken.type != Material.SAND || blockBroken.type != Material.RED_SAND) return
        drops.forEach(Consumer { drop: ItemStack ->
            if (drop.type == Material.SAND || drop.type == Material.RED_SAND) drop.setType(
                Material.GLASS
            )
        })
        for (i in drops.indices) {
            blockBroken.world.dropItemNaturally(blockBroken.location, drops.iterator().next())
        }
    }
}