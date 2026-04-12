package dev.lumas.lumaitems.items.armor.trousers

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class FlameguardGreavesItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#ba1e1e&lF&#bb211c&ll&#bb241a&la&#bc2618&lm&#bc2917&le&#bd2c15&lg&#a9351f&lu&#8a412f&la&#6b4d3f&lr&#4d594e&ld &#2e655e&lG&#1a6d69&lr&#1a6d69&le&#1a6d69&la&#1a6d69&lv&#1a6d69&le&#1a6d69&ls",
            mutableListOf("&#b81e1eP&#ba221by&#bc2619r&#bd2a16o&#bf2e13s&#ae371ch&#894630i&#645543e&#406357l&#1b726bd"),
            mutableListOf("&#ba1e1e\"&#ba1e1eT&#ba1e1eu&#ba1e1er&#ba1e1en &#ba1e1ef&#ba1e1ei&#ba1e1er&#ba1e1ee&#ad2424'&#9a2e2ds &#873736f&#74413fu&#604a48r&#4d5451y &#3a5d5at&#276763o &#1a6d69s&#1a6d69t&#1a6d69a&#1a6d69r&#1a6d69d&#1a6d69u&#1a6d69s&#1a6d69t&#1a6d69\"","","&fGrants fire resistance to the wearer","&fof these pants"),
            Material.NETHERITE_LEGGINGS,
            mutableListOf("flameguardgreaves"),
            mutableMapOf(Enchantment.PROTECTION to 7, Enchantment.BLAST_PROTECTION to 6, Enchantment.PROJECTILE_PROTECTION to 6, Enchantment.UNBREAKING to 8, Enchantment.UNBREAKING to 8, Enchantment.MENDING to 1)
        )
        return Pair("flameguardgreaves", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                player.addPotionEffect(PotionEffect(PotionEffectType.FIRE_RESISTANCE, 220, 0, false, false, false))
            }
            else -> return false
        }
        return true
    }
}