package dev.jsinco.luma.lumaitems.items.armor.elytra

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.awt.Color
import java.util.UUID
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class DevourersApronItem : CustomItemFunctions() {

    companion object {
        private val SOUL_ORBS: MutableMap<UUID, SoulOrb> = mutableMapOf()
        private val COLORS = listOf(
            "#800020", "#E54040", "#cd5c5d", "#d7a2a7", "#a45a7d"
        ).map { Color.decode(it) }
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#800020:#E54040:#cd5c5d:#d7a2a7:#a45a7d>Devourer’s Apron</gradient></b>")
            .customEnchants("<#cd5c5d>Consume")
            .material(Material.ELYTRA)
            .persistentData("devourers-apron")
            .tier(Tier.HALLOWEEN_2025)
            .lore(
                "Upon killing an entity,",
                "press your <#cd5c5d>swap key (F)</#cd5c5d>",
                "to consume their soul.",
                "",
                "Consuming a soul orb will",
                "regenerate and heal you",
                "past your maximum health.",
            )
            .vanillaEnchants(
                Enchantment.MENDING to 1,
                Enchantment.PROTECTION to 6,
                Enchantment.FIRE_PROTECTION to 4,
                Enchantment.FEATHER_FALLING to 5,
                Enchantment.UNBREAKING to 7
            )
            .buildPair()
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        SOUL_ORBS[player.uniqueId]?.remove()
        val soulOrb = SoulOrb(
            player = player,
            location = event.entity.eyeLocation.add(0.0, 0.1, 0.0),
            health = event.entity.getAttribute(Attribute.MAX_HEALTH)?.value?.toInt() ?: return
        )
        soulOrb.place()
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        val soulOrb = SOUL_ORBS.remove(player.uniqueId) ?: return
        if (soulOrb.consumable) {
            soulOrb.consume()
            event.isCancelled = true
        }
    }



    private class SoulOrb(
        val player: Player,
        val location: Location,
        val health: Int,
        val activeTicks: Int = 100, // 5 seconds
        val duration: Int = 3600, // 3 minutes
        val maxRange: Double = 70.0
    ) {

        companion object {
            private val REGEN = PotionEffect(PotionEffectType.REGENERATION, 100, 5, false, false, true)
        }

        init {
            SOUL_ORBS[player.uniqueId] = this
        }

        val particleDisplay: ParticleDisplay = ParticleDisplay.of(Particle.DUST_COLOR_TRANSITION)
            .withTransitionColor(COLORS.random(), 1f, COLORS.random())
            .withLocation(location)

        var consumable: Boolean = false
        var task: ScheduledTask? = null

        fun place() {
            if (this.consumable) return
            var ticksLived = 0

            this.consumable = true


            this.task = Executors.asyncTimer(0, 1) { task ->
                if (++ticksLived >= activeTicks) {
                    this.remove()
                    return@asyncTimer
                }

                Particles.sphere(0.2, 9.0, particleDisplay)
                playSounds()
            }
        }

        fun consume() {
            if (!this.consumable || player.location.distanceSquared(location) > maxRange * maxRange) return

            this.remove()

            this.consumeEffects {
                val amp = (health / 16).coerceAtMost(50)

                player.removePotionEffect(PotionEffectType.ABSORPTION)
                player.addPotionEffect(PotionEffect(PotionEffectType.ABSORPTION, duration, amp, false, false, false))
                if (health > 4) {
                    player.removePotionEffect(PotionEffectType.HEALTH_BOOST)
                    player.addPotionEffect(PotionEffect(PotionEffectType.HEALTH_BOOST, duration, amp, false, false, false))
                }
                player.addPotionEffect(REGEN)
                player.playSound(player.location, Sound.ENTITY_VEX_CHARGE, 0.4f, 1.2f)
            }
        }

        fun remove() {
            this.consumable = false
            this.task?.cancel()
            SOUL_ORBS.remove(this.player.uniqueId)

            this.location.world.spawnParticle(Particle.SCULK_SOUL, this.location, 10, 0.2, 0.2, 0.2, 0.0)
        }

        private fun consumeEffects(whenDone: () -> Unit) {
            val lineCurrentEnd: Location = location.clone()
            val lineCurrentStart: Location = location.clone()

            Executors.asyncTimer(0, 1) { task ->
                val toDestination = player.boundingBox.center.toLocation(player.world).clone().subtract(lineCurrentEnd)
                val distance = toDestination.length()

                if (distance < 1) {
                    task.cancel()
                    Executors.sync { whenDone() }
                    return@asyncTimer
                }

                val directionStep = toDestination.toVector().normalize().multiply(0.32)

                lineCurrentEnd.add(directionStep)
                lineCurrentEnd.add(directionStep)
                Particles.line(lineCurrentStart, lineCurrentEnd, 0.1, particleDisplay)
                playSounds()
                lineCurrentStart.add(directionStep)
                lineCurrentStart.add(directionStep)
            }
        }

        private fun playSounds() {
            if (Random.nextInt(10) == 5) {
                Executors.sync {
                    location.world.playSound(location, Sound.PARTICLE_SOUL_ESCAPE, 0.7f, 0.5f)
                }
            }
        }
    }
}