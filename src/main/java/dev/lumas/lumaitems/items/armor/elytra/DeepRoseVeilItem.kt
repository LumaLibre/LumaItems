package dev.lumas.lumaitems.items.armor.elytra

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.glowapi.model.GlowColorManager
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.tiers.Tier
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class DeepRoseVeilItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "deep-rose-veil".namespacedKey()
        private val GLOW = PotionEffect(PotionEffectType.GLOWING, 340, 0, false, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#560a35:#e79aa3:#fdd8e4:#7f587e:#583859>Deep Rose Veil</gradient></b>")
            .customEnchants("<#7B548C>Glow")
            .material(Material.ELYTRA)
            .tier(Tier.VALENTIDE_2026)
            .persistentData(KEY)
            .lore(
                "A delicate veil of",
                "petals wrapped up",
                "perfectly together.",
                "",
                "<#7B548C>While worn</#7B548C>, these wings",
                "will allow you to glow",
                "with a purple outline."
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
        GlowColorManager.getInstance().setTransientColor(player, NamedTextColor.DARK_PURPLE)
    }

    fun removeColor(player: Player) {
        GlowColorManager.getInstance().update(player)
    }
}