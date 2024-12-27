package dev.jsinco.luma.items.misc

import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SweetCandyItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#ce8efb&lS&#d891fa&lw&#e395f9&le&#ed98f8&le&#f89bf7&lt &#fda1f7&lC&#fea8f8&la&#feb0f9&ln&#ffb7fa&ld&#ffbffb&ly",
            mutableListOf("&7Haste II"),
            mutableListOf("This sweet candy just","really raises your blood sugar","","Holding this candy will","give you a Haste II boost"),
            Material.MAGENTA_DYE,
            mutableListOf("sweetcandy"),
            mutableMapOf(Enchantment.UNBREAKING to 10)
        )
        item.hideEnchants = true
        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"
        return Pair("sweetcandy", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                player.addPotionEffect(PotionEffect(PotionEffectType.HASTE, 220, 1, false, false, false))
            }
            else -> return false
        }
        return true
    }
}