package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.annotations.FireAnyways
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.flagFor
import dev.lumas.lumaitems.util.extensions.isFlagged
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

// TODO: These annotation are costly to use, I would not use this here.
//@FireAnyways(Action.PLAYER_DAMAGED)
class CrownJewelOfElsewhereItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "crown-jewel-of-elsewhere"
        private const val COOLDOWN_TICKS = 60L
        private const val EXPIRY_TICKS = 140L

        private val TRAIL_COLORS = listOf(
            Color.fromRGB(0x5d, 0x85, 0xdc), // cerulean
            Color.fromRGB(0xE5, 0x6A, 0x91), // rose
            Color.fromRGB(0xF3, 0xAA, 0x4C), // amber
            Color.fromRGB(0xCA, 0x51, 0xCB), // violet
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#5d85dc:#E56A91:#F3AA4C:#CA51CB>Crown Jewel of Elsewhere</gradient></b>")
            .customEnchants(
                "<gradient:#5d85dc:#CA51CB>Slip Between Worlds</gradient>",
                "<gray>Unbreaking</gray>" // this is intentionally not a vanilla enchantment
            )
            .vanillaEnchants(Enchantment.LOYALTY to 5)
            .hideEnchants(true)
            .lore(
                "A pearl that has slipped between",
                "worlds, always returning to",
                "its bearer's hand.",
                "",
                "May occasionally",
                "bring company.",
                "",
                "<red>Cooldown: 3s"
            )
            .material(Material.ENDER_PEARL)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.getCooldown(Material.ENDER_PEARL) > 0) return

        event.isCancelled = true

        val launchLoc = player.eyeLocation
        val pearl = player.launchProjectile(EnderPearl::class.java)
        pearl.setPersistentKey(KEY, PersistentDataType.SHORT, 1)

        player.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.7f, 1.5f)
        player.world.spawnParticle(Particle.PORTAL, launchLoc, 25, 0.2, 0.3, 0.2, 0.4)

        var trailTicks = 0
        Executors.asyncTimer(0, 1) { task ->
            if (pearl.isDead || !pearl.isValid || trailTicks >= EXPIRY_TICKS) {
                task.cancel()
                return@asyncTimer
            }
            trailTicks++
            val color = TRAIL_COLORS.random()
            pearl.world.spawnParticle(
                Particle.DUST,
                pearl.location, 3,
                0.05, 0.05, 0.05, 1.0,
                Particle.DustOptions(color, 1.3f), true
            )
            pearl.world.spawnParticle(Particle.END_ROD, pearl.location, 1, 0.0, 0.0, 0.0, 0.0, null, true)
        }

        player.setCooldown(Material.ENDER_PEARL, COOLDOWN_TICKS.toInt())
    }

    override fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {
        if (event.damageSource.damageType != DamageType.ENDER_PEARL) return
        if (player.isFlagged(this)) {
            event.isCancelled = true
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val loc = event.entity.location

        player.flagFor(this, 3)
        player.playSound(loc, Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 0.8f)
        player.playSound(loc, Sound.BLOCK_ENCHANTMENT_TABLE_USE, 0.5f, 1.3f)

        for (color in TRAIL_COLORS) {
            loc.world?.spawnParticle(
                Particle.DUST,
                loc, 12,
                0.35, 0.35, 0.35, 1.0,
                Particle.DustOptions(color, 1.5f)
            )
        }
        loc.world?.spawnParticle(Particle.PORTAL, loc, 40, 0.4, 0.4, 0.4, 0.5)
        loc.world?.spawnParticle(Particle.END_ROD, loc, 15, 0.2, 0.3, 0.2, 0.15)
    }

    override fun onPrepareCraft(player: Player, event: PrepareItemCraftEvent) {
        event.inventory.result = null
    }
}
