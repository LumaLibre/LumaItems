package dev.lumas.lumaitems.items.tools.spade

import com.destroystokyo.paper.MaterialTags
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.hasPersistentKey
import dev.lumas.lumaitems.util.extensions.itemStack
import dev.lumas.lumaitems.util.extensions.material
import dev.lumas.lumaitems.util.extensions.mix
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.toColor
import java.awt.Color
import java.util.concurrent.CompletableFuture
import kotlin.math.absoluteValue
import kotlin.random.Random
import org.bukkit.Color as BukkitColor
import org.bukkit.DyeColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class NimbusCrystalSpadeItem : CustomItemFunctions() {

    companion object {
        private val KEY = "nimbus-crystal-spade".namespacedKey()
        private val ACTIVATOR_KEY = "nimbus-crystal-spade-activator".namespacedKey()
        private val MATERIAL_NAME_KEY = "nimbus-crystal-spade-parent".namespacedKey()
        val BLUE_DUST = ParticleDisplay.of(Particle.DUST_COLOR_TRANSITION)
            .withTransitionColor(Color.decode("#AEC6CF"), 1.2f, Color.BLUE)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("nimbus crystal spade")
            .material(org.bukkit.Material.STONE_SHOVEL)
            .persistentData(KEY)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) return
        QuickTasks.addCooldown(this, player.uniqueId, 10)

        val snowball = player.launchProjectile(Snowball::class.java)
        snowball.setPersistentKey(ACTIVATOR_KEY, PersistentDataType.SHORT, 1)
        snowball.setPersistentKey(KEY, PersistentDataType.SHORT, 1)
        snowball.velocity = snowball.velocity.multiply(0.35)
        snowball.isPersistent = false
        snowball.item = MaterialTags.DYES.values.random().itemStack()


        Executors.asyncTimer(0, 1) { task ->
            if (!snowball.isValid) {
                task.cancel()
                return@asyncTimer
            }
            BLUE_DUST.spawn(snowball.location)
        }

        player.swingMainHand()
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return

        if (snowball.hasPersistentKey(ACTIVATOR_KEY)) {
            val rainCloud = RainCloud(player, snowball.location)
            rainCloud.seed().thenAccept {
                rainCloud.spawnCloud(300) {
                }
            }
            snowball.world.playSound(snowball.location, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.3f, 0.8f)
            return
        }

        val hitBlock = event.hitBlock ?: return
        val materialName = snowball.getPersistentKey(MATERIAL_NAME_KEY, PersistentDataType.STRING) ?: return
        val material = materialName.material() ?: return

        val sphere = Sphere(hitBlock.location, 1.0)
        sphere.getSphereFast {
            if (it.type != Material.AIR) {
                it.type = material
            }
        }

        hitBlock.breakNaturallyWithLog(player)

        hitBlock.syncDelayed(1) {
            hitBlock.location.getNearbyEntitiesByType(Item::class.java, 1.5, 1.0, 1.5)
                .forEach {
                    propelToPlayer(player, it)
                }
        }
    }

    private fun propelToPlayer(player: Player, item: Item) {
        val target = player.eyeLocation.clone().add(0.0, 0.3, 0.0)
        val origin = item.location

        val distance = target.distance(origin)

        if (distance < 1.0 && distance > -1.0) {
            return // don't apply if the item is too close
        }

        val direction = target.toVector().subtract(origin.toVector()).normalize()
        // if the items location is already above the player's head dont apply any Y
        if (origin.y > target.y) {
            direction.setY(0.0)
        } else {
            direction.setY(0.5 + (distance * 0.1))
        }

        val speed = 0.5
        item.velocity = direction.multiply(speed)
    }

    private class RainCloud(
        val shooter: Player,
        val spawnLoc: Location,
    ) {

        companion object {
            val PINK_SPARKLE = ParticleDisplay.of(Particle.INSTANT_EFFECT)
                .withColor(org.bukkit.Color.FUCHSIA.toColor())

            val DUST = ParticleDisplay.of(Particle.DUST)
            val snowballColors = DyeColor.entries.toList()
        }

        val cloudSpawnLoc = spawnLoc.clone().add(0.0, 11.0, 0.0)
        val cloudColor = DyeColor.entries.random()

        fun cloudColorConcretePowder(): Material {
            return "${cloudColor.name}_CONCRETE_POWDER".material() ?: Material.WHITE_CONCRETE_POWDER
        }


        fun seed(): CompletableFuture<Void> {
            val future = CompletableFuture<Void>()
            val lineCurrentEnd: Location = spawnLoc.clone()

            Executors.asyncTimer(0, 1) { task ->
                val toDestination = cloudSpawnLoc.clone().subtract(lineCurrentEnd)
                val distance = toDestination.length()

                if (distance < 1) {
                    task.cancel()
                    future.complete(null)
                    return@asyncTimer
                }

                val directionStep = toDestination.toVector().normalize().multiply(0.9)
                lineCurrentEnd.add(directionStep)
                Particles.line(spawnLoc, lineCurrentEnd, 0.35, PINK_SPARKLE)
            }
            return future
        }

        fun spawnCloud(ticks: Int, whenDone: () -> Unit) {
            val snowballs: MutableList<Snowball> = mutableListOf()
            val position = cloudSpawnLoc.clone()
            var count = 0

            Executors.asyncTimer(0, 1) { task ->
                if (++count > ticks) {
                    task.cancel()
                    whenDone()
                    return@asyncTimer
                }

                snowballs.removeIf { !it.isValid }

                val baseDir = shooter.eyeLocation.direction.clone().normalize()

                val pushVector = baseDir.multiply(0.075).apply {
                    y = 0.0
                }

                position.add(pushVector)

                position.world.spawnParticle(Particle.DUST, position, 200, 3.0, 1.0, 3.0,
                    Particle.DustOptions(cloudColor.fireworkColor.mix(BukkitColor.WHITE), 2f))


                val randLoc = position.clone().add(Random.nextDouble(-6.0, 6.0), 0.0, Random.nextDouble(-6.0, 6.0))


                snowballs.forEach {
                    val particleDisplaySpell = ParticleDisplay.of(Particle.INSTANT_EFFECT)
                        .withColor(cloudColor.fireworkColor.toColor())
                        .withExtra(0.0)
                    //DUST.withColor(colorForSnowball(it)).spawn(it.location)
                    particleDisplaySpell.spawn(it.location)
                }

                cloudSpawnLoc.sync {
                    snowball(randLoc).also {
                        snowballs.add(it)
                    }
                }

            }
        }


        private fun snowball(loc: Location): Snowball {
            val snowball = loc.world.createEntity(loc, Snowball::class.java)
            snowball.shooter = shooter
            snowball.isPersistent = false
            Util.setPersistentKey(snowball, KEY, PersistentDataType.SHORT, 1)
            snowball.setPersistentKey(MATERIAL_NAME_KEY, PersistentDataType.STRING, "${cloudColor.name}_CONCRETE_POWDER")

            snowball.spawnAt(loc)
            snowball.location.getNearbyPlayers(90.0).forEach {
                it.hideEntity(LumaItems.getInstance(), snowball)
            }
            return snowball
        }


        fun colorForSnowball(snowball: Snowball): Color {
            return snowballColors[snowball.entityId.hashCode().absoluteValue % snowballColors.size].color.toColor()
        }
    }
}