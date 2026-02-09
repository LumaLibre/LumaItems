package dev.lumas.lumaitems.items.armor.boots

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class BunnyBarillasItem : CustomItemFunctions() {

    companion object {
        private const val ITEM_KEY = "bunnybarillas"
        private val JUMP_BOOST = PotionEffect(PotionEffectType.JUMP_BOOST, 340, 2, false, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#E7934F>B<#EBA454>u<#EFB658>n<#F3C75D>n<#F7D861>y <#FBEA66>B<#FFFB6A>a<#E5F461>r<#CAED58>i<#B0E64F>l<#95DF45>l<#7BD83C>a<#60D133>s")
            .customEnchants("<#E7934F>Jump Boost III")
            .lore("Jump like a bunny!")
            .material(Material.NETHERITE_BOOTS)
            .persistentData(ITEM_KEY)
            .vanillaEnchants(
                Enchantment.PROTECTION to 7,
                Enchantment.PROJECTILE_PROTECTION to 6,
                Enchantment.FEATHER_FALLING to 7,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1
            )
            .tier(Tier.EASTER_2025)
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        if (Util.isItemInSlot(ITEM_KEY, EquipmentSlot.FEET, player)) {
            player.addPotionEffect(JUMP_BOOST)
        }
    }

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (Util.isItemInSlot(ITEM_KEY, EquipmentSlot.FEET, player)) {
            player.addPotionEffect(JUMP_BOOST)
        } else {
            player.removePotionEffect(PotionEffectType.JUMP_BOOST)
        }
    }
}