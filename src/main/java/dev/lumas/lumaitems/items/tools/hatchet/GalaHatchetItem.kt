package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.canBuild
import dev.lumas.lumaitems.util.extensions.hasPersistentKey
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toBukkitColor
import kotlin.math.cos
import kotlin.math.sin
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Firework
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class GalaHatchetItem : CustomItemFunctions() {

    private companion object {
        val ROCKET_KEY = "gala-hatchet-rocket".namespacedKey()

        const val MAX_ROCKETS = 9
        const val LAUNCH_STAGGER_TICKS = 2L
        const val FUSE_TICKS = 14
        const val HORIZONTAL_SPEED = 0.45
        const val VERTICAL_SPEED = 0.45
        const val BLAST_RADIUS = 2.0
        const val CROWDED_VOLLEY = 4
        const val CRAMPED_FLIGHT = 5.0
        const val TARGET_RADIUS = 8.0
        const val TARGET_ATTEMPTS = 4

        val COLORS = listOf(
            "#ff595e",
            "#ff924c",
            "#ffca3a",
            "#8ac926",
            "#36949d",
            "#1982c4",
            "#6a4c93",
            "#ff7bd5",
            "#ffffff",
        ).map { it.toBukkitColor() }

        val EFFECT_TYPES = listOf(
            FireworkEffect.Type.BALL,
            FireworkEffect.Type.BALL_LARGE,
            FireworkEffect.Type.STAR,
            FireworkEffect.Type.BURST,
        )

        val LIGHT_EFFECT_TYPES = listOf(
            FireworkEffect.Type.BALL,
            FireworkEffect.Type.BURST,
        )
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#ced9ef:#cfc7fb:#e2c6ee:#efc8df:#d7e9dc>Gala Hatchet</gradient></b>")
        .customEnchants("<#cfc7fb>Volley")
        .material(Material.NETHERITE_AXE)
        .tier(Tier.LUMARINE_2026)
        .persistentData("gala-hatchet")
        .vanillaEnchants(
            Enchantment.EFFICIENCY to 10,
            Enchantment.UNBREAKING to 5,
            Enchantment.MENDING to 1,
        )
        .lore(
            "A hatchet infused",
            "with pyro-technical",
            "abilities.",
            "",
            "<#cfc7fb>Broken</#Cfc7fb> logs will",
            "have a small chance",
            "to launch a variable",
            "amount of explosive",
            "fireworks."
        )
        .buildPair()

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        if (random.nextDouble() > 0.08 || player.isOnCooldown(this)) {
            return
        }

        launchVolley(player, event.block.location.toCenterLocation())
        player.addCooldown(this, 5)
    }

    override fun onPlayerDamagedByEntity(player: Player, event: EntityDamageByEntityEvent) {
        if (event.damager.hasPersistentKey(ROCKET_KEY)) {
            event.isCancelled = true
        }
    }

    private fun launchVolley(player: Player, origin: Location) {
        origin.world.playSound(origin, Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.5f, 1.0f)

        origin.syncDelayed(1) {
            val rockets = random.nextInt(1, MAX_ROCKETS + 1)
            val spin = random.nextDouble(Math.PI * 2)
            val logs = nearbyLogs(origin)

            repeat(rockets) { index ->
                val angle = spin + (Math.PI * 2 / rockets) * index + random.nextDouble(-0.15, 0.15)
                val direction = Vector(
                    cos(angle) * HORIZONTAL_SPEED,
                    VERTICAL_SPEED + random.nextDouble(-0.08, 0.12),
                    sin(angle) * HORIZONTAL_SPEED
                )
                val heading = aimAtLogs(origin, direction, logs) ?: direction

                origin.syncDelayed(index * LAUNCH_STAGGER_TICKS) {
                    launchRocket(player, origin, heading, rockets >= CROWDED_VOLLEY)
                }
            }
        }
    }

    private fun nearbyLogs(origin: Location): MutableList<Location> {
        val logs = mutableListOf<Location>()
        Sphere(origin, TARGET_RADIUS).getSphereFast { block ->
            if (block.type.isTagged(Tag.LOGS)) {
                logs.add(block.location.toCenterLocation())
            }
        }
        return logs
    }

    private fun aimAtLogs(origin: Location, direction: Vector, logs: MutableList<Location>): Vector? {
        val speed = direction.length()
        if (logs.isEmpty() || hitsWood(origin, direction, speed * FUSE_TICKS)) {
            return null
        }

        val candidates = logs.sortedBy { it.toVector().subtract(origin.toVector()).angle(direction) }
        for (target in candidates.take(TARGET_ATTEMPTS)) {
            val toTarget = target.toVector().subtract(origin.toVector())
            val heading = toTarget.clone().normalize().multiply(speed)

            if (!hitsWood(origin, heading, toTarget.length() + 1.0)) {
                continue
            }

            logs.remove(target)
            return heading
        }
        return null
    }

    private fun hitsWood(origin: Location, direction: Vector, reach: Double): Boolean {
        val hit = origin.world.rayTraceBlocks(origin, direction.clone().normalize(), reach) ?: return false
        val type = hit.hitBlock?.type ?: return false
        return type.isTagged(Tag.LOGS) || type.isTagged(Tag.LEAVES)
    }

    private fun launchRocket(player: Player, origin: Location, direction: Vector, crowdedVolley: Boolean) {
        val crowded = crowdedVolley || flightRoom(origin, direction) < CRAMPED_FLIGHT

        val firework = origin.world.spawn(origin, Firework::class.java) { firework ->
            val meta = firework.fireworkMeta
            meta.addEffect(randomEffect(crowded))
            meta.power = 0
            firework.fireworkMeta = meta

            firework.isShotAtAngle = true
            firework.shooter = player
            firework.ticksToDetonate = FUSE_TICKS + 5
            firework.setPersistentKey(ROCKET_KEY, PersistentDataType.BYTE, 1)
        }
        firework.velocity = direction

        var lastLocation = origin
        var ticksFlown = 0

        origin.syncTimer(1L, 1L) { task ->
            if (!firework.isValid || firework.isDetonated) {
                task.cancel()
                blastBlocks(player, lastLocation)
                return@syncTimer
            }

            lastLocation = firework.location
            if (++ticksFlown >= FUSE_TICKS) {
                task.cancel()
                firework.detonate()
                blastBlocks(player, lastLocation)
            }
        }
    }

    private fun blastBlocks(player: Player, center: Location) {
        if (!player.canBuild(center)) {
            return
        }

        Sphere(center, BLAST_RADIUS).getSphereFast { block ->
            if (block.isEmpty || block.isLiquid || (!block.type.isTagged(Tag.LOGS) && !block.type.isTagged(Tag.LEAVES))) {
                return@getSphereFast
            }
            // ragged edges
            if (block.location.toCenterLocation().distance(center) > BLAST_RADIUS - random.nextDouble(0.0, 0.8)) {
                return@getSphereFast
            }
            if (!player.canBuild(block.location)) {
                return@getSphereFast
            }

            block.breakNaturallyWithLog(player, false, false)
        }
    }


    private fun flightRoom(origin: Location, direction: Vector): Double {
        val reach = direction.length() * FUSE_TICKS
        val hit = origin.world.rayTraceBlocks(origin, direction.clone().normalize(), reach) ?: return reach
        return hit.hitPosition.distance(origin.toVector())
    }

    private fun randomEffect(crowded: Boolean): FireworkEffect {
        return FireworkEffect.builder()
            .with(if (crowded) LIGHT_EFFECT_TYPES.random() else EFFECT_TYPES.random())
            .withColor(COLORS.random(), COLORS.random(), COLORS.random())
            .withFade(COLORS.random(), COLORS.random())
            .trail(!crowded)
            .flicker(!crowded && random.nextBoolean())
            .build()
    }

}
