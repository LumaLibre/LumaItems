package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector

@Disable(value = [WorldName.SPAWN, WorldName.EVENT_NEW], hard = true)
class HeartstringHatchetItem : CustomItemFunctions() {

    companion object {
        private val still = Vector(0, 0, 0)
    }

    override fun createItem(): Pair<String, ItemStack> {
        val key = "heartstring-hatchet"
        val extra = 4.5 // survival range is 4.5
        return ItemFactory.builder()
            .name("<b><#FF8198>H<#FC8698>e<#F98C98>a<#F69199>r<#F29699>t<#EF9C99>s<#ECA199>t<#EBA4A9>r<#EAA6B9>i<#EAA9CA>n<#E9ABDA>g <#E9A8ED>H<#EBA1F0>a<#EC9BF3>t<#ED94F6>c<#EE8EF9>h<#F087FC>e<#F181FF>t")
            .customEnchants("<#FC8698>Extend I", "<#EF9C99>Vacuum")
            .material(Material.NETHERITE_AXE)
            .persistentData(key)
            .tier(Tier.VALENTIDE_2025)
            .lore(
                "Blocks and entities can",
                "be interacted with from",
                "further away using this",
                "hatchet.",
                "",
                "<#EBA1F0>Sneak & right-click<white> to",
                "vacuum up nearby items",
                "towards you.",
                "",
                "<red>Cooldown<gray>:<red> 40s"
            )
            .attributeModifiers(
                DefaultAttributes.NETHERITE_AXE.appendThenGetAttributes(
                    AttributeContainer.of(key, Attribute.BLOCK_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, extra, EquipmentSlotGroup.MAINHAND),
                    AttributeContainer.of(key, Attribute.ENTITY_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, extra, EquipmentSlotGroup.MAINHAND)
                )
            )
            .vanillaEnchants(
                Enchantment.SMITE to 6,
                Enchantment.BANE_OF_ARTHROPODS to 6,
                Enchantment.FORTUNE to 4,
                Enchantment.EFFICIENCY to 8,
                Enchantment.UNBREAKING to 14,
                Enchantment.MENDING to 1
            )
            .spoofEnchants(true)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (!player.isSneaking || QuickTasks.isOnCooldown(this, player.uniqueId)) return
        QuickTasks.addCooldown(this, player.uniqueId, 800L)
        object : BukkitRunnable() {

            var i = 0

            override fun run() {
                val originLocation = player.eyeLocation
                val nearbyItems = player.location.world?.getNearbyEntities(player.location, 10.0, 10.0, 10.0)
                    ?.filterIsInstance<Item>() ?: run {
                    this.cancel()
                    return
                }

                if (i++ > 100) {
                    nearbyItems.forEach { it.velocity = still }
                    this.cancel()
                }

                nearbyItems.forEach { item ->
                    val direction: Vector = originLocation.clone().subtract(item.location).toVector()
                    val distance: Double = direction.x + direction.y + direction.z


                    if (distance > 0.6 || distance < -0.6) {
                        // Create a vector which spins the items around the player
                        item.velocity = direction.normalize().multiply(0.4)
                            .rotateAroundY(1.1)
                    }
                }
            }
        }.runTaskTimer(instance(), 0L, 5L)

    }
}