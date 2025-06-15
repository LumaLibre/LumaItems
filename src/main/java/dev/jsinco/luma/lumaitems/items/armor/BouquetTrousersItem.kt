package dev.jsinco.luma.lumaitems.items.armor

import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import java.util.UUID

class BouquetTrousersItem : CustomItemFunctions() {

    companion object {
        // The fastest way to do this is to use a list that's going to be clean most of the time
        private val tracked: MutableList<UUID> = mutableListOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val key = "bouquet-trousers"
        return ItemFactory.builder()
            .name("<b><#FAA4DF>B<#F796D4>o<#F388C8>u<#F07ABD>q<#ED6DB1>u<#EA5FA6>e<#E6519A>t <#E27191>T<#E09F93>r<#DFCD94>o<#DDFB96>u<#CCF493>s<#BAEC8F>e<#A9E58C>r<#97DD88>s")
            .vanillaEnchants(
                Enchantment.UNBREAKING to 7,
                Enchantment.PROTECTION to 9,
                Enchantment.SWIFT_SNEAK to 4,
                Enchantment.MENDING to 1
            )
            .customEnchants("<#DDFB96>Lift")
            .persistentData(key)
            .lore(
                "While falling, <#DDFB96>sneak<white> to",
                "perform a double jump."
            )
            .tier(Tier.VALENTIDE_2025)
            .material(Material.NETHERITE_LEGGINGS)
            .attributeModifiers(
                DefaultAttributes.NETHERITE_LEGGINGS.appendThenGetAttributes(
                    AttributeContainer.of(key, Attribute.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_NUMBER, 0.025, EquipmentSlotGroup.LEGS)
                )
            )
            .buildPair()
    }

    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        if (player.isSneaking || player.isFlying || AbilityUtil.isOnGround(player) || tracked.contains(player.uniqueId)) {
            return
        }
        tracked.add(player.uniqueId)
        player.velocity = player.location.direction.multiply(0.6).setY(0.7)
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (!tracked.contains(player.uniqueId) || !AbilityUtil.isOnGround(player)) {
            return
        }
        tracked.remove(player.uniqueId)
    }

    override fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {
        if (tracked.contains(player.uniqueId)) {
            event.isCancelled = true
        }
    }
}