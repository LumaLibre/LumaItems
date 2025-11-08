package dev.jsinco.luma.lumaitems.items.weapons.scythe

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.BukkitVectors
import dev.jsinco.luma.lumaitems.util.Executors
import java.awt.Color
import java.util.UUID
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

class NightmareScytheItem : CustomItemFunctions() {

    companion object {
        private val REFERENCES: MutableMap<UUID, Seize> = mutableMapOf()
        private val COLORS = listOf("#ad7abf", "#d2672d", "#d49662", "#6c3f2e", "#302c2f").map { Color.decode(it) }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("Nightmare Scythe")
            .customEnchants("Seize")
            .persistentData("nightmare-scythe")
            .material(Material.NETHERITE_HOE)
            .buildPair()
    }



    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val foundEntity = player.getTargetEntity(30) as? LivingEntity
        val loc = foundEntity?.eyeLocation?.add(0.0, 1.5, 0.0)
            ?: player.getTargetBlockExact(10)?.location?.add(0.0, 3.0, 0.0)
            ?: player.location.add(player.location.direction.multiply(10)).add(0.0, 2.0, 0.0)
        val particleDisplay = ParticleDisplay.of(Particle.DUST)
            .withColor(COLORS.random())

        val seize = Seize(
            player = player,
            pin = loc,
            particleDisplay = particleDisplay,
            passedEntityType = foundEntity?.type,
            radius = 6.0,
            durationTicks = 100L
        )

        seize.startTicking()
    }

    private class Seize(
        val player: Player,
        val pin: Location,
        val particleDisplay: ParticleDisplay,
        val passedEntityType: EntityType?,
        val radius: Double = 8.0,
        val durationTicks: Long = 150
    ) {



        var task: BukkitTask? = null

        fun startTicking() {
            var entities: List<LivingEntity> = pin.world.getNearbyLivingEntities(pin, radius)
                .filter {it !is Player && !AbilityUtil.noDamagePermission(player, it) }
            val entityType = (passedEntityType ?: entities.firstOrNull()?.type)?.also { entityType ->
                entities = entities.filter { it.type == entityType }
            }

            val newDisplay = particleDisplay.clone()
                .withLocation(pin)

            Particles.spikeSphere(0.2, 30.0, 50, 1.0, 3.0, newDisplay)
            if (entities.isEmpty() || entityType == null) {
                return
            }

            REFERENCES[player.uniqueId] = this


            var count = 0

            pin.world.playSound(pin, Sound.ITEM_LEAD_BREAK, 2.0f, Random.Default.nextDouble(0.5, 0.8).toFloat())

            this.task = Executors.syncTimer(0, 1) { task ->
                if (++count > durationTicks || entities.all { it.isDead }) {
                    Particles.spikeSphere(0.2, 30.0, 50, 1.0, 3.0, newDisplay)
                    this.stopTicking()
                    return@syncTimer
                }

                pin.getNearbyLivingEntities(radius).forEach {
                    if (it.type == entityType && it !in entities && !AbilityUtil.noDamagePermission(player, it)) {
                        entities = entities + it
                    }
                }

                //Particles.sphere(0.1, 10.0, newDisplay)
                Particles.spikeSphere(0.1, 30.0, 30, 0.5, 3.0, newDisplay)

                for (entity in entities) {
                    val newVel = BukkitVectors.seizeToAnchor(entity, pin, 1.0)
                    if (newVel != null) {
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
        }

        fun stopTicking() {
            this.task?.cancel()
            REFERENCES.remove(player.uniqueId)
        }
    }
}