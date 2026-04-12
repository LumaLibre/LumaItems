package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.extensions.flag
import dev.lumas.lumaitems.util.extensions.getFlag
import dev.lumas.lumaitems.util.extensions.isFlagged
import dev.lumas.lumaitems.util.extensions.isLocationOnGround
import dev.lumas.lumaitems.util.extensions.removeFlag
import dev.lumas.lumaitems.util.extensions.toColor
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack

class PalomasStridesItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#911025:#c44a58:#f2cbb8:#ffbd9a:#b18e57:#2b1b15>Paloma's Strides</gradient></b>")
            .customEnchants("<#c44a58>Bloom")
            .material(Material.NETHERITE_LEGGINGS)
            .lore(
                "Not yet written."
            )
            .tier(Tier.WONDERLAND_2026.alt())
            .persistentData("palomas-strides")
            .buildPair()
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (!player.isFlagged(this) || !player.isLocationOnGround()) return
        player.removeFlag(this)
    }

    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        if (player.isSneaking || player.isInWater || player.isFlying || player.isLocationOnGround()) {
            return
        }

        val boostCount = player.getFlag(this, Int::class.java) ?: 0
        if (boostCount < 5) {
            player.velocity = player.location.direction.multiply(0.6).setY(0.83)
            player.flag(this, boostCount + 1)

            // effects
            val particleDisplay = ParticleDisplay.of(Particle.INSTANT_EFFECT)
                .withColor("#911025".toColor())
                .withLocation(player.location)

            Particles.flower(3, 0.5, particleDisplay) {
                Particles.circle(0.5, 5.0, particleDisplay)
            }
            player.world.playSound(player.location, Sound.ENTITY_ALLAY_ITEM_TAKEN, 0.3f, 1f)
        }
    }
}