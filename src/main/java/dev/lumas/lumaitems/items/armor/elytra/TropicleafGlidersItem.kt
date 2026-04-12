package dev.lumas.lumaitems.items.armor.elytra

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack


class TropicleafGlidersItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "tropicleaf-gliders"
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#E58B8B:#efb484:#E2E787:#B3CA8E>Tropicleaf Gliders</gradient></b>")
            .customEnchants("<#E58B8B>Absorption")
            .persistentData(KEY)
            .material(Material.ELYTRA)
            .tier(Tier.SUMMER_2025)
            .lore(
                "Made from the finest",
                "tropical leaves, these",
                "wings absorb <#E58B8B>15%</#E58B8B> of",
                "all incoming damage.",
            )
            .vanillaEnchants(
                Enchantment.BLAST_PROTECTION to 5,
                Enchantment.FIRE_PROTECTION to 5,
                Enchantment.UNBREAKING to 8,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {
        if (!Util.isItemInSlot(KEY, EquipmentSlot.CHEST, player)) return
        val damageReduction = event.damage * 0.15
        event.damage -= damageReduction


        // Tick durability
        val item = player.equipment?.chestplate ?: return
        item.damage(1, player)
    }
}