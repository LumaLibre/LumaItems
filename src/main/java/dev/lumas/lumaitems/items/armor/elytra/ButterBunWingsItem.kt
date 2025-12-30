package dev.lumas.lumaitems.items.armor.elytra

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class ButterBunWingsItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#F15795&lB&#EB6992&lu&#E47C8F&lt&#DE8E8D&lt&#D8A18A&le&#D1B387&lr&#CBC684&lB&#C3D182&lu&#B8D480&ln &#ADD77E&lW&#A2DB7D&li&#97DE7B&ln&#8CE279&lg&#81E577&ls",
            mutableListOf("&7Glow I"),
            mutableListOf("A wing given by the spring","fairies to help the bunnies","find easter eggs!"),
            Material.ELYTRA,
            mutableListOf("butterbunwings"),
            mutableMapOf(Enchantment.UNBREAKING to 7, Enchantment.PROTECTION to 6, Enchantment.FEATHER_FALLING to 5, Enchantment.MENDING to 1)
        )
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("butterbunwings", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                if (Util.isItemInSlot("butterbunwings", EquipmentSlot.CHEST, player)) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 340, 0, false, false, false))
                }
            }
            Action.ARMOR_CHANGE -> {
                if (!Util.isItemInSlot("butterbunwings", EquipmentSlot.CHEST, player)) {
                    player.removePotionEffect(PotionEffectType.GLOWING)
                } else {
                    player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 340, 0, false, false, false))
                }
            }
            else -> return false
        }
        return true
    }

}
