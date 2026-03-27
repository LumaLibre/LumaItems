package dev.lumas.lumaitems.items.weapons.trident

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class NeptunianLanceItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#51f9b9&lN&#69f9c2&le&#80f9cb&lp&#98f9d4&lt&#aff9dd&lu&#c7f9e6&ln&#def9ef&li&#def9ef&la&#c7f9e6&ln &#aff9dd&lL&#98f9d4&la&#80f9cb&ln&#69f9c2&lc&#51f9b9&le",
            mutableListOf("&#51f9b9G&#67f9c1r&#7df9caa&#93f9d2c&#a8f9dbi&#bef9e3o&#d4f9ecu&#eaf9f4s"),
            mutableListOf(
                "&#51f9b9\"&#56f9bbD&#5bf9bdr&#60f9bfe&#65f9c1a&#6af9c2m &#6ff9c4b&#73f9c6i&#78f9c8g&#7df9cag&#82f9cce&#87f9cer &#8cf9d0t&#91f9d1h&#96f9d3a&#9bf9d5n &#a0f9d7t&#a5f9d9h&#aaf9dbe &#aef9ddo&#b3f9dfc&#b8f9e0e&#bdf9e2a&#c2f9e4n&#c7f9e6\"",
                "",
                "&fUsing this trident grants",
                "&fthe wielder dolphin's grace"
            ),
            Material.TRIDENT,
            mutableListOf("neptunianlance"),
            mutableMapOf(
                Enchantment.IMPALING to 8,
                Enchantment.RIPTIDE to 4,
                Enchantment.SHARPNESS to 8,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1
            )
        )
        return Pair("neptunianlance", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 220, 0, false, false, false))
            }
            else -> return false
        }
        return true
    }
}