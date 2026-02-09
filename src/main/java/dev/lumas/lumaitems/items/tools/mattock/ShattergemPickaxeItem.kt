package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import dev.lumas.lumaitems.util.disabling.Disable
import dev.lumas.lumaitems.util.disabling.WorldName
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class ShattergemPickaxeItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#5D5BF8&lS&#6863F6&lh&#726AF4&la&#7D72F2&lt&#8879F0&lt&#9281EE&le&#9D88EC&lr&#A790EA&lg&#B297E8&le&#AF97E8&lm &#AB98E8&lP&#A898E8&li&#A598E8&lc&#A198E8&lk&#9E99E8&la&#9A99E8&lx&#9799E8&le",
            mutableListOf("&#9799E8Forgiving Touch"),
            mutableListOf("Grants the user the ability to", "silk touch budding amethyst"),
            Material.NETHERITE_PICKAXE,
            mutableListOf("shattergempickaxe"),
            mutableMapOf(Enchantment.EFFICIENCY to 7, Enchantment.SILK_TOUCH to 1, Enchantment.UNBREAKING to 10, Enchantment. MENDING to 1)
        )
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("shattergempickaxe", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.BREAK_BLOCK -> {
                event as BlockBreakEvent
                if (event.block.type == Material.BUDDING_AMETHYST && player.gameMode != GameMode.CREATIVE) {
                    event.block.world.dropItemNaturally(event.block.location, ItemStack(Material.BUDDING_AMETHYST))
                }
            }
            else -> return false
        }
        return true
    }
}