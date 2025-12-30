package dev.lumas.lumaitems.items.tools.harrow

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.QuickTasks
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Animals
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class BelovedFallowItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><#ffa5e3>B<#ffafe6>e<#ffb8e9>l<#ffc2ed>o<#ffcbf0>v<#ffd5f3>e<#ffdef6>d <#f9d8f8>F<#f4d1f9>a<#eecbfb>l<#e8c5fc>l<#e3befe>o<#ddb8ff>w")
            .customEnchants("<#ddb8ff>Breeder")
            .lore(
                "<#ffa5e3>Right-click<white> while holding",
                "to breed animals in a <#ffa5e3>5x5",
                "radius around you.",
                "",
                "<red>Cooldown<gray>:<red> 2m"
            )
            .material(Material.NETHERITE_HOE)
            .persistentData("belovedfallow")
            .vanillaEnchants(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 10, Enchantment.EFFICIENCY to 6, Enchantment.FORTUNE to 5)
            .tier(Tier.VALENTIDE_2025)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            return
        }
        val playerLocation = player.location
        val entities = playerLocation.world.getNearbyEntities(playerLocation, 5.0, 5.0, 5.0)

        var affected = 0
        for (entity in entities) {
            if (entity is Animals) {
                if (!entity.canBreed()) {
                    continue
                }

                entity.world.spawnParticle(Particle.HEART, entity.location, 4, 0.2, 0.5, 0.2, 0.0)
                entity.loveModeTicks = 600 // Normal breeding time
                affected++
            }
        }

        if (affected == 0) {
            return
        }
        QuickTasks.addCooldown(this, player.uniqueId, 2400L)
    }
}