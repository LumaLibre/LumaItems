package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class PastelShearsItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#E690E7&lP&#E79DC9&la&#E8A9AA&ls&#E9B68C&lt&#E9C27A&le&#E7CD80&ll &#E5D886&lS&#E3E38C&lh&#D5E895&le&#C1EA9F&la&#AEECAA&lr&#9AEEB4&ls",
            mutableListOf("&#E79DC9Glasscutter"),
            mutableListOf("Breaks all types of", "glass instantly."),
            Material.SHEARS,
            mutableListOf("pastelshears"),
            mutableMapOf(Enchantment.UNBREAKING to 9, Enchantment.MENDING to 1, Enchantment.EFFICIENCY to 6, Enchantment.SILK_TOUCH to 1)
        )
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("pastelshears", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.LEFT_CLICK -> {
                if (!Util.isItemInSlot("pastelshears", EquipmentSlot.HAND, player)) {
                    return false
                }
                event as PlayerInteractEvent
                val material = event.clickedBlock?.type ?: return false

                if (material.name.contains("GLASS")) {
                    player.breakBlock(event.clickedBlock ?: return false)
                }
            }
            else -> return false
        }
        return true
    }
}