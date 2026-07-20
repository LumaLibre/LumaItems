package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toColor
import kotlin.math.min
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

class UmbraScimitarItem : CustomItemFunctions() {

    private companion object {
        const val DURATION_TICKS = 200
        const val COOLDOWN_TICKS = 400L
        const val PULL_RADIUS = 6.0
        const val PULL_STRENGTH = 0.12
        const val PULL_DAMAGE = 1.0
        const val DAMAGE_INTERVAL = 20
        const val BURST_STRENGTH = 1.8

        val blackDust = ParticleDisplay.of(Particle.DUST)
            .withColor("#210B2F".toColor())
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#210B2F:#420d4b:#7b347f:#8665C7:#c774b2:#f4d5e0>Umbra Scimitar</gradient></b>")
        .customEnchants("<#8665C7>Singularity")
        .persistentData("umbra-scimitar")
        .material(Material.NETHERITE_SWORD)
        .tier(Tier.LUMARINE_2026)
        .vanillaEnchants(Enchantment.SHARPNESS to 8,
            Enchantment.SMITE to 8,
            Enchantment.LOOTING to 5,
            Enchantment.SWEEPING_EDGE to 4,
            Enchantment.UNBREAKING to 10,
            Enchantment.MENDING to 1)
        .lore(
            //""
            //""
        )
        .buildPair()

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player)) return

        val location = player.getTargetBlockExact(50)?.location?.add(0.0, 1.1, 0.0) ?: return
        QuickTasks.addCooldown(this, player, COOLDOWN_TICKS)

        val particleDisplay = ParticleDisplay.of(Particle.COPPER_FIRE_FLAME)
            .withLocation(location)
            .directional()
            .withExtra(0.1)

        Particles.blackhole(instance, 16, 3.0, 40.0, 1, DURATION_TICKS, particleDisplay)

        location.world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 1.6f)

        var elapsed = 0
        location.syncTimer(1, 1) { task ->
            if (elapsed++ >= DURATION_TICKS) {
                burst(location, player)
                task.cancel()
                return@syncTimer
            }

            Particles.sphere(0.3, 3.0, blackDust.clone().withLocation(location))

            val damageTick = elapsed % DAMAGE_INTERVAL == 0
            for (victim in victimsAround(location, player)) {
                val toCenter = location.toVector().subtract(victim.location.toVector())
                val distance = toCenter.length()
                if (distance > 0.1) {
                    val pull = toCenter.normalize().multiply(PULL_STRENGTH * min(1.0, distance / 2.0))
                    victim.velocity = victim.velocity.multiply(0.85).add(pull)
                }
                if (damageTick) {
                    victim.damage(PULL_DAMAGE, player)
                }
            }

            if (elapsed % 40 == 0) {
                location.world.playSound(location, Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 0.5f)
            }
        }
    }

    private fun burst(center: Location, player: Player) {
        val world = center.world
        world.spawnParticle(Particle.EXPLOSION_EMITTER, center, 1)
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.7f)

        for (victim in victimsAround(center, player)) {
            val outward = victim.location.toVector().subtract(center.toVector()).setY(0.0)
            if (outward.lengthSquared() < 0.01) {
                outward.x = random().nextDouble(-1.0, 1.0)
                outward.z = random().nextDouble(-1.0, 1.0)
            }
            outward.normalize()
                .add(Vector(random().nextDouble(-0.5, 0.5), 0.0, random().nextDouble(-0.5, 0.5)))
                .normalize()
                .multiply(BURST_STRENGTH)
                .setY(random().nextDouble(0.4, 0.9))
            victim.velocity = outward
        }
    }

    private fun victimsAround(center: Location, player: Player): List<LivingEntity> {
        return center.getNearbyLivingEntities(PULL_RADIUS).filter {
            it != player && it !is ArmorStand && !it.isDead
        }
    }
}
