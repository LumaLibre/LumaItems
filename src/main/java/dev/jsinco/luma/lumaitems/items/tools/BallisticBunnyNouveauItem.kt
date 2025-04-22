package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import java.awt.Color
import java.util.UUID
import kotlin.math.exp

class BallisticBunnyNouveauItem : CustomItemFunctions() {

    companion object {
        private const val MAX_EXPLOSION_POWER = 40.0
        private val cachedCharges: MutableMap<UUID, Short> = mutableMapOf()
        private val colors: List<Color> = listOf(
            "#fe9999", "#fecaec", "#8cd3ff",
            "#a6dca2", "#fffcbf"
        ).map { Util.hex2AwtColor(it) }
    }



    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#fe9999:#fecaec:#8cd3ff:#a6dca2:#fffcbf>Ballistic Bunny Nouveau</gradient></b>")
            .customEnchants("<#fe9999>Explosive!")
            .material(Material.NETHERITE_PICKAXE)
            .tier(Tier.EASTER_2025)
            .persistentData("ballistic-bunny-nouveau")
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 9,
                Enchantment.UNBREAKING to 9,
                Enchantment.FORTUNE to 5,
                Enchantment.KNOCKBACK to 5,
                Enchantment.MENDING to 1
            )
            .lore(
                "With this mattock, break",
                "blocks to charge it up. When",
                "you're ready, press your",
                "<#fecaec>swap key (F)</#fecaec> to unleash",
                "an explosion.",
                "",
                "<red>Cooldown: 1m"
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        var charge = cachedCharges.getOrPut(player.uniqueId) { 0 }
        if (charge++ == Short.MAX_VALUE) {
            this.createExplosion(player, true)
            charge = 0
        }
        cachedCharges[player.uniqueId] = charge
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        this.createExplosion(player, false)
        event.isCancelled = true
    }

    private fun createExplosion(player: Player, bypassCooldown: Boolean) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId) && !bypassCooldown) {
            player.sendActionBar(MiniMessageUtil.mm("<gray>›.‹ <red>I can't explode right now!"))
            return
        }
        val power = getExplosionPower(player)
        if (power < 4.0) {
            player.sendActionBar(MiniMessageUtil.mm("<gray>›.‹ <#a6dca2>I don't have enough explosion power!"))
            return
        }

        val particleDisplay = ParticleDisplay.of(Particle.DUST)
            .withLocation(player.location)
            .withColor(colors.random())

        cachedCharges[player.uniqueId] = 0
        QuickTasks.addCooldown(this, player.uniqueId, 1200L)
        Particles.meguminExplosion(instance(), power / 10.0, particleDisplay)
        Bukkit.getScheduler().runTaskLater(instance(), Runnable {
            player.world.createExplosion(player, power.toFloat(), false)
        }, 5L)
    }

    private fun getExplosionPower(player: Player): Double {
        val charge = cachedCharges[player.uniqueId] ?: return 0.0
        val calculated = exponentialFit(charge)
        if (calculated > MAX_EXPLOSION_POWER) {
            return MAX_EXPLOSION_POWER
        }
        return calculated
    }

    private fun exponentialFit(x: Short): Double {
        // I'm using: f(x) = A−B⋅e^-k(x-x0)
        val A = 100.0 // Max value we want to approach
        val B = A - 4.0 // A - f(100)
        val k = 0.0000199 // Controls the curve steepness
        val x0 = 100 // Shifts curve to match x=100

        return A - B * exp(-k * (x - x0))
    }
}