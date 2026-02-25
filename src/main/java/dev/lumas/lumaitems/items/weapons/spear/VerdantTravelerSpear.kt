package dev.lumas.lumaitems.items.weapons.spear

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isItemInSlots
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityExhaustionEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class VerdantTravelerSpear : CustomItemFunctions() {

    private companion object {
        private val KEY = Util.namespacedKey("verdant-traveler-spear")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#B9E6C9:#A7DCEB:#F3E3B1:#CDE7B5>Verdant Traveler</gradient></b>")
            .customEnchants("<#A7DCEB>Endurance")
            .material(Material.NETHERITE_SPEAR)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "Built for the journey,",
                "not the destination.",
                "",
                "<#A7DCEB>Lunge</#A7DCEB> forward without",
                "draining your hunger."
            )
            .vanillaEnchants(
                // Max VANILLA enchants, no more
                Enchantment.LUNGE to 3,
                Enchantment.SHARPNESS to 5,
                Enchantment.FIRE_ASPECT to 2,
                Enchantment.KNOCKBACK to 2,
                Enchantment.LOOTING to 3,
                Enchantment.UNBREAKING to 3,
                Enchantment.MENDING to 1,
            )
            .buildPair()
    }

    override fun onEntityExhaustion(player: Player, event: EntityExhaustionEvent) {
        if (player.isFlying || !player.isItemInSlots(KEY, EquipmentSlot.HAND, EquipmentSlot.OFF_HAND)) return
        if (event.exhaustionReason != EntityExhaustionEvent.ExhaustionReason.ENCHANTMENT_EFFECT) return
        event.exhaustion = 0.0f
    }
}