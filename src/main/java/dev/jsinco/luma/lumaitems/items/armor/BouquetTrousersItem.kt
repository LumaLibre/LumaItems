package dev.jsinco.luma.lumaitems.items.armor

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
            .name("<#FAA4DF>B<#F58ACD>o<#EF6FBB>u<#EA55A8>q<#E43A96>e<#E3438F>t <#E05482>T<#DF7E87>r<#DFA88C>o<#DED191>u<#DDFB96>s<#C6F191>e<#AEE78D>r<#97DD88>s")
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
                AttributeContainer.of(key, Attribute.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_NUMBER, 0.025, EquipmentSlotGroup.LEGS),
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
}