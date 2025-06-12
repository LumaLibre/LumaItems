package dev.jsinco.luma.lumaitems.items.armor

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.disabling.Ignore
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Ignore
class OGBunnyBarillasItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
                "OG &#E7934F&lB&#EBA454&lu&#EFB658&ln&#F3C75D&ln&#F7D861&ly &#FBEA66&lB&#FFFB6A&la&#E5F461&lr&#CAED58&li&#B0E64F&ll&#95DF45&ll&#7BD83C&la&#60D133&ls",
                mutableListOf("&#E7934FJump Boost III"),
                mutableListOf("Jump like a bunny!"),
                Material.NETHERITE_BOOTS,
                mutableListOf("bunnybarillas"),
                mutableMapOf(Enchantment.PROTECTION to 6, Enchantment.PROJECTILE_PROTECTION to 7, Enchantment.FEATHER_FALLING to 5, Enchantment.UNBREAKING to 8, Enchantment.MENDING to 1)
        )
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("bunnybarillas", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                if (Util.isItemInSlot("bunnybarillas", EquipmentSlot.FEET, player)) {
                    player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 340, 2, false, false, false))
                }
            }
            Action.ARMOR_CHANGE -> {
                if (!Util.isItemInSlot("bunnybarillas", EquipmentSlot.FEET, player)) {
                    player.removePotionEffect(PotionEffectType.JUMP_BOOST)
                } else {
                    player.addPotionEffect(PotionEffect(PotionEffectType.JUMP_BOOST, 340, 2, false, false, false))
                }
            }
            else -> return false
        }
        return true
    }
}