package dev.jsinco.luma.lumaitems.items.armor

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.ThanksgivingEventTier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class FoliageWingsItem : CustomItemFunctions() {

    companion object {
        private const val ID = "foliagewings"
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#645B82>F<#A2979A>o<#E0D2B2>l<#E6B77E>i<#D99877>a<#CC7870>g<#CC7870>e <#f6f0e4>Wings</b>")
            .customEnchants("<#645B82>Glow")
            .persistentData(ID)
            .material(Material.ELYTRA)
            .lore("Glide from the trees,", "just like an autumn leaf!")
            .tier(ThanksgivingEventTier.THANKSGIVING_2024)
            .vanillaEnchants(Enchantment.PROTECTION to 9, Enchantment.UNBREAKING to 7, Enchantment.MENDING to 1)
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        if (Util.isItemInSlot(ID, EquipmentSlot.CHEST, player)) {
            player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 340, 0, false, false, false))
        }
    }

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (!Util.isItemInSlot(ID, EquipmentSlot.CHEST, player)) {
            player.removePotionEffect(PotionEffectType.GLOWING)
        } else {
            player.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 340, 0, false, false, false))
        }
    }
}