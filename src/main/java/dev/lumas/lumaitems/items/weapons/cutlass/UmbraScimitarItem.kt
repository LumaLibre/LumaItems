package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.canDamage
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toColor
import io.papermc.paper.entity.Leashable
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import kotlin.math.min
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Tameable
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector

class UmbraScimitarItem : CustomItemFunctions() {

    private companion object {
        const val DURATION_TICKS = 200
        const val COOLDOWN_TICKS = 600L
        const val PULL_STRENGTH = 0.15
        const val PULL_DAMAGE = 8.0
        const val DAMAGE_INTERVAL = 20
        const val BURST_STRENGTH = 1.8
        const val RADIUS = 16.0
        const val PULL_RADIUS = 15.0
        const val SEIZE_RELEASE_RADIUS = 5.0
        const val BURST_DAMAGE = 25.0

        val colors = listOf(
            "#210B2F",
            "#420d4b",
            "#7b347f",
            "#8665C7",
            "#c774b2",
            "#f4d5e0"
        ).map { it.toColor() }

        val dust = ParticleDisplay.of(Particle.DUST)
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#210B2F:#420d4b:#7b347f:#8665C7:#c774b2:#f4d5e0>Umbra Scimitar</gradient></b>")
        .customEnchants("<#5F2165>Singularity")
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
            "<#5F2165>Right-click</#5F2165> to summon a",
            "gravity well at a targeted",
            "block.",
            "",
            "Nearby entities will be",
            "pulled and weakened.",
            "",
            "<red>Cooldown: 30s"
        )
        .buildPair()

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isOnCooldown(this)) return

        val targetBlock = player.getTargetBlockExact(75) ?: return
        val yAdd = if (targetBlock.isSolid) 1.2 else 0.1
        val location = targetBlock.location.add(0.5, yAdd, 0.5)

        player.addCooldown(this, COOLDOWN_TICKS)

        val particleDisplay = ParticleDisplay.of(Particle.COPPER_FIRE_FLAME)
            .withLocation(location)
            .directional()
            .withExtra(0.1)
        val preparedDust = dust.clone().withColor(colors.random())

        Particles.blackhole(instance, 16, 3.0, 40.0, 1, DURATION_TICKS, particleDisplay)

        location.world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 0.8f, 1.6f)

        var elapsed = 0
        location.syncTimer(1, 1) { task ->
            if (elapsed++ >= DURATION_TICKS) {
                task.cancel()
                burst(location, player)
                return@syncTimer
            }

            Particles.sphere(0.3, 3.0, preparedDust.withLocation(location))

            val damageTick = elapsed % DAMAGE_INTERVAL == 0
            for (victim in victimsAround(location, player)) {
                if (victim is LivingEntity && !player.canDamage(victim)) {
                    task.cancel() // just break now
                    return@syncTimer
                }

                if (victim.location.distanceSquared(location) <= PULL_RADIUS * PULL_RADIUS) {
                    val toCenter = location.toVector().subtract(victim.location.toVector())
                    val distance = toCenter.length()
                    if (distance > 0.1) {
                        val pull = toCenter.normalize().multiply(PULL_STRENGTH * min(1.0, distance / 2.0))
                        victim.velocity = victim.velocity.multiply(0.85).add(pull)
                    }
                } else if (victim is LivingEntity) {
                    val seize = Seize(player, victim, location, elapsed, preparedDust)
                    seize.pull()
                }


                if (damageTick && victim != player) {
                    (victim as? LivingEntity)?.damage(PULL_DAMAGE, player)
                    victim.velocity = BukkitVectors.ZERO
                }
            }

            if (elapsed % 40 == 0) {
                location.world.playSound(location, Sound.BLOCK_PORTAL_AMBIENT, 0.5f, 0.5f)
            }
        }
    }

    private fun burst(center: Location, player: Player) {
        val world = center.world
        world.spawnParticle(Particle.FLAME, center, 50, 0.5, 0.5, 0.5, 0.5)
        world.playSound(center, Sound.ENTITY_GENERIC_EXPLODE, 1.2f, 0.7f)

        for (victim in victimsAround(center, player).filter { it is LivingEntity }) {
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
            (victim as? LivingEntity)?.let {
                it.damage(BURST_DAMAGE, player)
                it.fireTicks = 200
            }
            victim.velocity = outward
        }
    }

    private fun victimsAround(center: Location, player: Player): List<Entity> {
        return center.getNearbyEntities(RADIUS, RADIUS, RADIUS).filter {
            it !is Player
                    && it !is Fireball
                    && it !is ArmorStand
                    && !it.isDead
                    && (it !is Tameable || !it.isTamed)
                    && (it !is Leashable || !it.isLeashed)
                    && it.customName() == null
        }
    }


    private class Seize(
        val player: Player,
        val entity: LivingEntity,
        val pin: Location,
        val remainingTicks: Int,
        val particleDisplay: ParticleDisplay,
    ) {

        var task: ScheduledTask? = null

        fun pull() {
            val remainingTicks = this.remainingTicks - 10
            if (entity.isDead || remainingTicks <= 1) {
                return
            }

            var count = 0

            pin.world.playSound(pin, Sound.ITEM_LEAD_BREAK, 2.0f, Random.nextDouble(0.5, 0.8).toFloat())

            this.task = pin.syncTimer(0, 1) { task ->
                if (++count > remainingTicks || entity.isDead || entity.location.distanceSquared(pin) < SEIZE_RELEASE_RADIUS * SEIZE_RELEASE_RADIUS) {
                    this.stop()
                    return@syncTimer
                }

                BukkitVectors.seizeToAnchor(entity, pin, 2.0)?.let { newVel ->
                    entity.velocity = newVel
                }

                val loc = entity.boundingBox.center.toLocation(entity.world)
                Particles.line(pin, loc, 0.4, particleDisplay)


                if (count % 10 == 0) {
                    entity.damage(5.0, player)
                    val center = entity.boundingBox.center.toLocation(entity.world)
                    entity.world.spawnParticle(Particle.WAX_OFF, center, 3, 0.2, 0.2, 0.2, 0.9)
                }
            }
        }

        fun stop() {
            this.task?.cancel()
        }
    }
}
