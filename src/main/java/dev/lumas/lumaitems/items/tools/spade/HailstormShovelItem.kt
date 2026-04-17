package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.tags.Kind
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.Tier
import java.awt.Color
import kotlin.math.abs
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector

class HailstormShovelItem : CustomItemFunctions() {

    companion object {
        private val COLORS = listOf("#EEBEC5", "#D9C0EF", "#A6C6EC").map { Util.hex2AwtColor(it) }.plus(Color.WHITE)
        private val PARTICLE_DISPLAY = ParticleDisplay.of(Particle.DUST)
        private val WHITE_PARTICLE_DISPLAY = PARTICLE_DISPLAY.clone().withColor(Color.WHITE)
        private val ITEM_STACK = ItemStack(Material.WIND_CHARGE)
        private val key = Util.namespacedKey("hailstorm-shovel")
        private val SHOVEL_ITEM_STACK = ItemStack(Material.NETHERITE_SHOVEL).apply {
            addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1)
        }
        private const val ACTIVATOR = "activator"
    }

    private val fallingProjectile = fun(loc: Location, shooter: Player): Snowball {
        val projectile = loc.world.spawn(loc, Snowball::class.java)
        projectile.shooter = shooter
        projectile.item = ITEM_STACK
        Util.setPersistentKey(projectile, key, PersistentDataType.SHORT, 0)
        return projectile
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#EEBEC5:#D9C0EF:#A6C6EC>Hailstorm Shovel</gradient></b>")
            .customEnchants("<#A6C6EC>Hailing Geyser")
            .material(Material.NETHERITE_SHOVEL)
            .persistentData(key)
            .tier(Tier.SUMMER_2025)
            .lore(
                "<#A6C6EC>Right-click</#A6C6EC> to summon a",
                "storm cloud that rains",
                "down a torrent of hail.",
                "",
                "Hail from the storm will",
                "break blocks in its path",
                "and propel dropped",
                "items toward you.",
                "",
                "<red>Cooldown: 3m"
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 7,
                Enchantment.UNBREAKING to 7,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId) || player.location.block.lightFromSky < 1) {
            return
        }
        QuickTasks.addCooldown(this, player.uniqueId, 3600) // 3 min
        val snowball = player.launchProjectile(Snowball::class.java)
        snowball.velocity = snowball.velocity.multiply(0.3)
        snowball.setMetadata(ACTIVATOR, FixedMetadataValue(instance(), true))
        snowball.item = ITEM_STACK
        Util.setPersistentKey(snowball, key, PersistentDataType.SHORT, 1)
        player.swingMainHand()
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val projectile = event.entity
        if (projectile.hasMetadata(ACTIVATOR)) {
            cloudSeed(projectile) {
                hailStorm(projectile.location, player)
            }
            return
        }

        val hitEntity = event.hitEntity
        if (hitEntity is Player) {
            event.isCancelled = true
        } else if (hitEntity is LivingEntity && !AbilityUtil.noDamagePermission(player, hitEntity)) {
            hitEntity.damage(5.0, player)
        }

        val block = event.hitBlock ?: return

        if (!block.isPreferredTool(SHOVEL_ITEM_STACK) || Kind.BLACKLIST.isTagged(block.type) || AbilityUtil.noBuildPermission(player, block)) {
            return
        }

        block.breakNaturallyWithLog(player, SHOVEL_ITEM_STACK)
        block.location.syncDelayed(1) {
            block.location.getNearbyEntitiesByType(Item::class.java, 1.5, 1.0, 1.5)
                .forEach {
                    propelToPlayer(player, it)
                }
        }
    }

    private fun cloudSeed(entity: Projectile, whenDone: () -> Unit) {

        val position = if (!entity.isInWater) {
            entity.location
        } else {
            // keep going up until we find a non-water block
            val blockIterator = BlockIterator(entity.world, entity.location.toVector(), Vector(0.0, 1.0, 0.0), 0.0, 20)
            while (blockIterator.hasNext()) {
                val next = blockIterator.next()
                if (next.type.isAir) {
                    next.location
                }
            }
            entity.location // fallback if no block found
        }
        val destination = position.clone().add(0.0, 11.0, 0.0)


        val lineCurrentEnd: Location = position.clone()

        Executors.asyncTimer(0, 1) { task ->

            val toDestination = destination.clone().subtract(lineCurrentEnd)
            val distance = toDestination.length()

            if (distance > 1) {
                val directionStep = toDestination.toVector().normalize().multiply(0.9)
                lineCurrentEnd.add(directionStep)
            } else {
                task.cancel()
                whenDone()
            }

            Particles.line(position, lineCurrentEnd, 0.35, WHITE_PARTICLE_DISPLAY)
        }
    }

    private fun hailStorm(loc: Location, player: Player) {
        val spawnLocation = loc.add(0.0, 10.0, 0.0)
        val direction = player.location.direction.clone().normalize()


        val snowballs = mutableListOf<Projectile>()
        var count = 0
        Executors.asyncTimer(0, 1) { task ->
            if (count++ > 200) {
                task.cancel()
                return@asyncTimer
            }

            snowballs.removeIf { it.isDead }
            // Move cloud 0.1 blocks per tick in horizontal direction
            val stormVector = direction.clone()
            val yFactor = stormVector.y / 2
            stormVector.x += yFactor
            stormVector.z += yFactor
            stormVector.setY(0).multiply(0.1)
            spawnLocation.add(stormVector)


            loc.world.spawnParticle(
                Particle.CLOUD,
                spawnLocation,
                60,
                3.0, 0.0, 3.0,
                0.0
            )

            val randLoc = spawnLocation.clone().add(random().nextDouble(-6.0, 6.0), 0.0, random().nextDouble(-6.0, 6.0))

            snowballs.forEach { projectile ->
                // have it determine a random color based on the location

                val x = projectile.location.blockX
                val z = projectile.location.blockZ

                // Hash the x and z to get a stable pseudo-random index
                val index = abs((31 * x + z).hashCode()) % COLORS.size
                val color = COLORS[index]
                PARTICLE_DISPLAY.withColor(color)
                    .spawn(projectile.location)
            }

            spawnLocation.sync {
                val snowball = fallingProjectile(randLoc, player)
                //snowball.velocity = direction.clone().multiply(0.2)
                // go in the player's facing direction
                snowballs.add(snowball)
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
}