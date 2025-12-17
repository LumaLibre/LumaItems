package dev.jsinco.luma.lumaitems.items.tools.spade

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.enums.BlockConstants
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class ColorCrystalSpadeItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#EE9393&lC&#EE9D90&lo&#EDA68C&ll&#EDB089&lo&#EDBC85&lr &#EDC982&lC&#EDD67E&lr&#E0DD7E&ly&#CCE17F&ls&#B7E480&lt&#A8E38E&la&#A3DBAE&ll &#9DD3CE&lS&#9ECAE8&lp&#B8BAE8&la&#D2ABE9&ld&#EC9BE9&le",
            mutableListOf("&#ec9be9Style"),
            mutableListOf("Breaking sand of any kind will", "automatically convert it to", "smelted and colored glass"),
            Material.NETHERITE_SHOVEL,
            mutableListOf("colorcrystalspade"),
            mutableMapOf(Enchantment.EFFICIENCY to 7, Enchantment.UNBREAKING to 8, Enchantment.SILK_TOUCH to 1, Enchantment.MENDING to 1)
        )
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("colorcrystalspade", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.BREAK_BLOCK -> {
                event as BlockBreakEvent
                val block = event.block
                if (block.type == Material.SAND || block.type == Material.RED_SAND) {
                    block.world.dropItemNaturally(block.location, ItemStack(BlockConstants.COLORED_GLASS.materials.random()))
                    event.isDropItems = false
                }
            }
            else -> return false
        }
        return true
    }

}