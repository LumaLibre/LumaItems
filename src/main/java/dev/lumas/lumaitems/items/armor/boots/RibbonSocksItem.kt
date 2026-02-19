package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setRemainingHealth
import dev.lumas.lumaitems.util.extensions.willBreak
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class RibbonSocksItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "ribbon-socks".namespacedKey()
        private val DAMAGE_TYPE = listOf(
            EntityDamageEvent.DamageCause.FALL,
            EntityDamageEvent.DamageCause.FREEZE
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#daa1f4:#ffcfff:#d3fff9:#aba4ff>Ribbon Socks</gradient></b>")
            .customEnchants("<#daa1f4>Lightweight")
            .material(Material.NETHERITE_BOOTS)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "A pair of lovely",
                "socks, perfect for",
                "any cold day.",
                "",
                "<#daa1f4>Wearing</#daa1f4> these socks",
                "prevents all types",
                "of fall damage."
            )
            .vanillaEnchants(
                Enchantment.UNBREAKING to 4,
                Enchantment.SOUL_SPEED to 3,
                Enchantment.DEPTH_STRIDER to 3,
                Enchantment.BLAST_PROTECTION to 5,
                Enchantment.PROJECTILE_PROTECTION to 6,
                //Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {
        if (player.isItemInSlot(KEY, EquipmentSlot.FEET) && DAMAGE_TYPE.contains(event.cause)) {
            val item = player.equipment?.boots ?: return
            if (!item.willBreak(1)) {
                item.damage(event.damage.div(3.0).toInt(), player)
                event.isCancelled = true
            } else {
                player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 0.65f, 1.0f)
            }
        }
    }

    override fun onPlayerItemDamage(player: Player, event: PlayerItemDamageEvent) {
        val item = event.item
        if (item.willBreak(event.damage)) {
            item.setRemainingHealth(1)
            event.isCancelled = true
        }
    }

}