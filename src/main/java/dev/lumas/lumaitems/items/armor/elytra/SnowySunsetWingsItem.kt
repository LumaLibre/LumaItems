@file:Suppress("deprecation")
package dev.lumas.lumaitems.items.armor.elytra

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.glowapi.colormanagers.ColorManager
import dev.lumas.lumaitems.hooks.McMMOHook
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SnowySunsetWingsItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("snowy-sunset-wings")
        private val GLOW = PotionEffect(PotionEffectType.GLOWING, 340, 0, false, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#33394f:#767da4:#d8cde0:#b6a5bf:#dfbbc1>Snowy Sunset Wings</gradient></b>")
            .customEnchants("<#33394f>Color Glow")
            .persistentData(KEY)
            .material(Material.ELYTRA)
            .tier(Tier.CHRISTMAS_2025)
            .lore(
                "Soar through the sky,",
                "lit by the soft glow",
                "of a snowy sunset.",
                "",
                "<#33394f>While worn</#33394f>, these wings",
                "will allow you to glow",
                "with a black outline."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 6,
                Enchantment.FEATHER_FALLING to 5,
                Enchantment.UNBREAKING to 7,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        if (player.isItemInSlot(KEY, EquipmentSlot.CHEST)) {
            player.addPotionEffect(GLOW)
        }
    }


    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (player.isItemInSlot(KEY, EquipmentSlot.CHEST)) {
            player.addPotionEffect(GLOW)
            addColor(player)
        } else {
            player.removePotionEffect(PotionEffectType.GLOWING)
            removeColor(player)
        }
    }

    override fun onPlayerJoin(player: Player, event: PlayerJoinEvent) {
        addColor(player)
    }

    fun addColor(player: Player) {
        if (Registry.HOOKS.getOrThrow(McMMOHook::class).isWith()) {
            ColorManager.setTempPlayerColor(player, ChatColor.BLACK)
        }
    }

    fun removeColor(player: Player) {
        if (Registry.HOOKS.getOrThrow(McMMOHook::class).isWith()) {
            ColorManager.updatePlayersColor(player)
        }
    }
}