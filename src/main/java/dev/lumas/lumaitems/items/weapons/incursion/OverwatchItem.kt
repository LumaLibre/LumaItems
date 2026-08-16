package dev.lumas.lumaitems.items.weapons.incursion

import dev.lumas.core.util.Text
import dev.lumas.lumaitems.items.weapons.incursion.IncursionArsenal.BEAM_STEP
import dev.lumas.lumaitems.items.weapons.incursion.IncursionArsenal.Target
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncTimer
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.time.Duration
import java.util.LinkedHashMap
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.roundToInt
import net.kyori.adventure.title.Title
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class OverwatchItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "incursion-overwatch"

        private const val CHARGE_TICKS = 16
        private const val COOLDOWN_TICKS = 45
        private const val RANGE = 100.0
        private const val HIT_RADIUS = 0.1
        private const val DAMAGE = 40.0
        private const val HEADSHOT_MULTIPLIER = 2.0

        private const val CHARGE_BAR_SEGMENTS = 10

        private const val ACTIVATION_GRACE_TICKS = 10
        private const val CHARGE_GIVE_UP_TICKS = 20 * 60

        private val BEAM_DUST = Particle.DustOptions(Color.fromRGB(255, 85, 255), 0.6f)
        private val CHARGE_TITLE_TIMES: Title.Times =
            Title.Times.times(Duration.ZERO, Duration.ofMillis(400), Duration.ZERO)
    }

    private val charging = ConcurrentHashMap<UUID, ScheduledTask>()

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#9b8fe0:#c07fd6:#d97fc0>Overwatch</gradient></b>")
            .vanillaEnchants(Enchantment.PIERCING to 10)
            .customEnchants("<#c774b2>Sightline")
            .lore(
                "<#c774b2>Hold</#c774b2> to charge, <#c774b2>release</#c774b2> to",
                "fire. Heads count double.",
                "",
                "Built to spot things on the",
                "horizon. It has since learned",
                "to do something about them.",
                "",
                "<red>Cooldown: 2.25s"
            )
            .material(Material.SPYGLASS)
            .persistentData(KEY)
            .unbreakable(true)
            .tier(Tier.LUMARINE_2026)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.item?.isMatchingItem(KEY) != true) return
        if (player.hasCooldown(Material.SPYGLASS)) {
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 0.5f)
            return
        }

        watchCharge(player)
    }

    private fun watchCharge(player: Player) {
        charging.remove(player.uniqueId)?.cancel()

        var charged = false
        var seenActive = false
        var ticks = 0
        val slot = player.inventory.heldItemSlot

        val poller = player.syncTimer(1, 1) { task ->
            ticks++
            val holding = player.isValid &&
                player.hasActiveItem() &&
                player.activeItem.isMatchingItem(KEY)

            if (!holding) {
                if (!seenActive && ticks < ACTIVATION_GRACE_TICKS) return@syncTimer

                task.cancel()
                charging.remove(player.uniqueId, task)
                if (!seenActive) return@syncTimer

                player.clearTitle()

                val stillHeld = player.isValid &&
                    player.inventory.heldItemSlot == slot &&
                    player.inventory.itemInMainHand.isMatchingItem(KEY)
                if (!stillHeld) return@syncTimer

                if (charged) {
                    fire(player)
                } else {
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 0.8f, 0.6f)
                }
                return@syncTimer
            }
            seenActive = true

            if (ticks > CHARGE_GIVE_UP_TICKS) {
                task.cancel()
                charging.remove(player.uniqueId, task)
                player.clearTitle()
                return@syncTimer
            }

            val held = player.activeItemUsedTime
            if (held >= CHARGE_TICKS) {
                if (!charged) {
                    charged = true
                    player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f)
                }
                player.showTitle(
                    Title.title(Text.mm("<green><b>⌄"), Text.mm("<green><b>READY"), CHARGE_TITLE_TIMES)
                )
                return@syncTimer
            }

            val filled = ((held.toDouble() / CHARGE_TICKS) * CHARGE_BAR_SEGMENTS).roundToInt()
            player.showTitle(
                Title.title(
                    Text.mm("<yellow><b>⌄"),
                    Text.mm("<yellow>" + "|".repeat(filled) + "<dark_gray>" + "|".repeat(CHARGE_BAR_SEGMENTS - filled)),
                    CHARGE_TITLE_TIMES
                )
            )
        }

        if (poller != null) charging[player.uniqueId] = poller
    }

    private fun fire(player: Player) {
        player.setCooldown(Material.SPYGLASS, COOLDOWN_TICKS)

        val eye = player.eyeLocation
        val direction = eye.direction.normalize()

        eye.world.playSound(eye, Sound.ENTITY_GENERIC_EXPLODE, 1f, 0.6f)
        eye.world.playSound(eye, Sound.ITEM_TRIDENT_THROW, 1.1f, 0.5f)
        eye.world.playSound(eye, Sound.ENTITY_WARDEN_SONIC_BOOM, 0.35f, 1.4f)

        advanceBeam(player, eye, direction, 0.0, LinkedHashMap())
    }

    private fun advanceBeam(
        player: Player,
        eye: Location,
        direction: Vector,
        startDistance: Double,
        targets: LinkedHashMap<UUID, Target>
    ) {
        val world = eye.world
        val origin = eye.toVector()
        var step = 0
        var lastX = Int.MIN_VALUE
        var lastY = Int.MIN_VALUE
        var lastZ = Int.MIN_VALUE
        var lastChunkX = Int.MIN_VALUE
        var lastChunkZ = Int.MIN_VALUE
        var drawnTo = startDistance

        var distance = startDistance
        while (distance <= RANGE) {
            val x = eye.x + (direction.x * distance)
            val y = eye.y + (direction.y * distance)
            val z = eye.z + (direction.z * distance)

            val blockX = floor(x).toInt()
            val blockY = floor(y).toInt()
            val blockZ = floor(z).toInt()
            val chunkX = blockX shr 4
            val chunkZ = blockZ shr 4

            if (!Bukkit.isOwnedByCurrentRegion(world, chunkX, chunkZ)) {
                val resumeAt = distance
                Bukkit.getRegionScheduler().run(instance, world, chunkX, chunkZ) {
                    advanceBeam(player, eye, direction, resumeAt, targets)
                }
                return
            }

            if (chunkX != lastChunkX || chunkZ != lastChunkZ) {
                lastChunkX = chunkX
                lastChunkZ = chunkZ
                for (target in IncursionArsenal.targetsInChunk(player, world, chunkX, chunkZ)) {
                    targets[target.entity.uniqueId] = target
                }
            }

            // Steps can land in the same block more than once
            val sameBlock = blockX == lastX && blockY == lastY && blockZ == lastZ
            lastX = blockX
            lastY = blockY
            lastZ = blockZ

            val stoppedAt = if (sameBlock) {
                -1.0
            } else {
                IncursionArsenal.solidHitDistance(world.getBlockAt(blockX, blockY, blockZ), origin, direction, RANGE)
            }

            if (stoppedAt >= 0) {
                // Fences and walls collide above their own block
                val impactAt = max(stoppedAt, drawnTo)
                val impact = Location(
                    world,
                    eye.x + (direction.x * impactAt),
                    eye.y + (direction.y * impactAt),
                    eye.z + (direction.z * impactAt)
                )

                IncursionArsenal.forced(world, Particle.EXPLOSION, impact, 1, 0.0, 0.0)
                IncursionArsenal.forced(world, Particle.CRIT, impact, 8, 0.1, 0.25)
                world.playSound(impact, Sound.ENTITY_GENERIC_EXPLODE, 0.4f, 1.9f)

                applyBeamDamage(player, eye, direction, impactAt, targets)
                return
            }

            IncursionArsenal.forced(world, Particle.DUST, x, y, z, 1, 0.0, 0.0, BEAM_DUST)
            if (step % 4 == 0) {
                IncursionArsenal.forced(world, Particle.END_ROD, x, y, z, 1, 0.0, 0.0)
            }

            drawnTo = distance
            distance += BEAM_STEP
            step++
        }

        applyBeamDamage(player, eye, direction, RANGE, targets)
    }

    private fun applyBeamDamage(
        player: Player,
        eye: Location,
        direction: Vector,
        maxDistance: Double,
        targets: Map<UUID, Target>
    ) {
        var hits = 0
        val headshots = mutableListOf<Double>()

        for (target in targets.values) {
            val hitbox = target.expandedHitbox(HIT_RADIUS)
            val maxReach = maxDistance + hitbox.height
            if (eye.toVector().distanceSquared(hitbox.center) > maxReach * maxReach) continue

            if (!IncursionArsenal.beamTouches(hitbox, eye, direction, maxDistance)) continue

            val headshot = IncursionArsenal.beamTouches(target.headHitbox(HIT_RADIUS), eye, direction, maxDistance)
            IncursionArsenal.hurt(target.entity, player, if (headshot) DAMAGE * HEADSHOT_MULTIPLIER else DAMAGE)

            if (headshot) {
                headshotEffect(target)
                headshots.add(target.headHitbox(0.0).center.distance(eye.toVector()))
            }
            hits++
        }

        if (hits == 0) return
        player.sync {
            if (headshots.isEmpty()) IncursionArsenal.hitFeedback(player)
            else IncursionArsenal.headshotFeedback(player, headshots)
        }
    }

    private fun headshotEffect(target: Target) {
        val entity = target.entity
        val centre = target.headHitbox(0.0).center

        entity.sync {
            val world = entity.world
            val at = centre.toLocation(world)
            IncursionArsenal.forced(world, Particle.CRIT, at, 12, 0.15, 0.35)
            world.playSound(at, Sound.ENTITY_PLAYER_ATTACK_CRIT, 1f, 1.2f)
        }
    }
}
