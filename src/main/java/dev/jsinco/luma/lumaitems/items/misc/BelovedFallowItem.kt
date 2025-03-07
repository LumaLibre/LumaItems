package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Animals
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class BelovedFallowItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
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

//        val item = ItemFactory(
//            "<b><#ffa5e3>B<#ffafe6>e<#ffb8e9>l<#ffc2ed>o<#ffcbf0>v<#ffd5f3>e<#ffdef6>d <#f9d8f8>F<#f4d1f9>a<#eecbfb>l<#e8c5fc>l<#e3befe>o<#ddb8ff>w",
//            mutableListOf("<#ddb8ff>Breeder"),
//            mutableListOf("Right-click while holding", "to breed animals in a 5x5", "radius around you", "", "&cCooldown: 2m"),
//            Material.NETHERITE_HOE,
//            mutableListOf("belovedfallow"),
//            mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 10, Enchantment.EFFICIENCY to 6, Enchantment.FORTUNE to 5)
//        )
//        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"
//        return Pair("belovedfallow", item.createItem())