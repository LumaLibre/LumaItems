package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.UUID

class WinterVaultItem : CustomItemFunctions() {

    companion object {
        private val cooldown: MutableSet<UUID> = mutableSetOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#CBD8E8>W<#B3B1CA>i<#9B8AAC>n<#C9ACBB>t<#F7CEC9>e<#F5D7C7>r <#F0E8C3>V<#DFE1B2>a<#CDDAA0>u<#B0D6B4>l<#93D2C8>t</b>")
            .customEnchants("<#CCD8E9>Launch I")
            .lore("Right-click to be propelled", "into the air", "", "<red>Cooldown: 15s")
            .material(Material.BREEZE_ROD)
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.KNOCKBACK to 4)
            .tier(Tier.WINTER_2024)
            .persistentData("wintervault")
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (cooldown.contains(player.uniqueId)) {
            return
        }

        player.velocity = player.velocity.add(Vector(0.0, 1.5, 0.0)).multiply(2.45)
        player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f)

        cooldown.add(player.uniqueId)
        Bukkit.getServer().scheduler.scheduleSyncDelayedTask(instance(), { cooldown.remove(player.uniqueId) }, 300L)

        object : BukkitRunnable() {
            private var ticks = 0

            override fun run() {
                player.world.spawnParticle(Particle.GUST, player.location, 2, 0.3, 0.3, 0.3, 0.1)
                ticks++
                if (ticks >= 50) {
                    this.cancel()
                }
            }
        }.runTaskTimer(instance(), 0L, 1L)
    }
}