package dev.lumas.lumaitems.items.weapons.incursion

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.PaperDataComponent
import dev.lumas.lumaitems.util.Tier
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.TooltipDisplay
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import kotlin.math.max
import kotlin.math.min
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.World
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class SpitterItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "incursion-spitter"

        private const val COOLDOWN_TICKS = 5
        private const val SPEED = 1.2
        private const val GRAVITY = 0.06
        private const val LAUNCH_ANGLE_DEGREES = 10.0
        private const val RANGE = 22.0
        private const val HIT_RADIUS = 0.15
        private const val DAMAGE = 40.0
        private const val DAMAGE_FALLOFF = 0.25
        private const val TRAIL_STEPS = 2

        private val SPIT_DUST = Particle.DustOptions(Color.fromRGB(70, 145, 230), 0.5f)
    }

    override fun createItem(): Pair<String, ItemStack> {
        val (key, item) = ItemFactory.builder()
            .name("<b><gradient:#52b9d9:#5fcbd2:#74cfae>Spitter</gradient></b>")
            .vanillaEnchants(Enchantment.AQUA_AFFINITY to 10)
            .customEnchants("<#57c6e1>Waterjet")
            .lore(
                "<#57c6e1>Right-click</#57c6e1> to squeeze out",
                "a pressurised jet of brine.",
                "",
                "The fish is not happy",
                "about this arrangement.",
            )
            .material(Material.PUFFERFISH)
            .maxStackSize(1)
            .persistentData(KEY)
            .tier(Tier.LUMARINE_2026)
            .paperDataComponents(
                PaperDataComponent.valued(
                    DataComponentTypes.TOOLTIP_DISPLAY,
                    TooltipDisplay.tooltipDisplay()
                        .addHiddenComponents(DataComponentTypes.ATTRIBUTE_MODIFIERS)
                        .build()
                )
            )
            .buildPair()

        item.unsetData(DataComponentTypes.CONSUMABLE)
        item.unsetData(DataComponentTypes.FOOD)
        return key to item
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.item?.isMatchingItem(KEY) != true) return

        event.setUseItemInHand(Event.Result.DENY)
        event.isCancelled = true

        if (player.hasCooldown(Material.PUFFERFISH)) {
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_HAT, 0.7f, 0.5f)
            return
        }
        fire(player)
    }

    private fun fire(player: Player) {
        player.setCooldown(Material.PUFFERFISH, COOLDOWN_TICKS)

        val muzzle = IncursionArsenal.mainHandLocation(player)
        val aim = player.eyeLocation
        aim.pitch = max(-90.0, aim.pitch - LAUNCH_ANGLE_DEGREES).toFloat()
        val velocity = aim.direction.normalize().multiply(SPEED)

        muzzle.world.playSound(muzzle, Sound.ENCHANT_THORNS_HIT, 0.9f, 1.5f)
        muzzle.world.playSound(muzzle, Sound.ENTITY_PUFFER_FISH_BLOW_OUT, 0.5f, 1.3f)
        IncursionArsenal.forced(muzzle.world, Particle.SPLASH, muzzle, 6, 0.05, 0.02)
        IncursionArsenal.forced(muzzle.world, Particle.DUST, muzzle, 6, 0.06, 0.0, SPIT_DUST)

        advanceSpit(player, muzzle, velocity, 0.0)
    }

    private fun advanceSpit(player: Player, position: Location, velocity: Vector, travelledSoFar: Double) {
        if (!player.isValid || travelledSoFar >= RANGE) return

        val world = position.world
        val stepVector = velocity.clone().multiply(1.0 / TRAIL_STEPS)
        val stepLength = stepVector.length()

        val stepDirection = if (stepLength > 1.0E-6) stepVector.clone().multiply(1.0 / stepLength) else null
        val targets = IncursionArsenal.targetsAround(player, position, (stepLength * TRAIL_STEPS) + 3.0)
        var travelled = travelledSoFar

        val point = position.clone()
        for (step in 0 until TRAIL_STEPS) {
            val from = point.toVector()
            point.add(stepVector)
            travelled += stepLength

            if (!Bukkit.isOwnedByCurrentRegion(point)) {
                val resumeAt = point.clone()
                val resumeTravelled = travelled
                Bukkit.getRegionScheduler().run(instance, resumeAt) {
                    advanceSpit(player, resumeAt, velocity, resumeTravelled)
                }
                return
            }

            if (stepDirection != null &&
                IncursionArsenal.solidHitDistance(world.getBlockAt(point), from, stepDirection, stepLength) >= 0
            ) {
                splash(world, point)
                return
            }

            for (target in targets) {
                if (!target.containsWithin(HIT_RADIUS, point.x, point.y, point.z)) continue

                IncursionArsenal.hurt(
                    target.entity, player,
                    DAMAGE * (1.0 - (DAMAGE_FALLOFF * min(1.0, travelled / RANGE)))
                )

                splash(world, point)
                player.sync { IncursionArsenal.hitFeedback(player) }
                return
            }

            IncursionArsenal.forced(world, Particle.SPLASH, point, 1, 0.0, 0.0)
            IncursionArsenal.forced(world, Particle.DUST, point, 2, 0.05, 0.0, SPIT_DUST)
            if (step % 2 == 0) {
                IncursionArsenal.forced(world, Particle.FALLING_WATER, point, 1, 0.06, 0.0)
            }

            if (travelled >= RANGE) {
                splash(world, point)
                return
            }
        }

        val nextVelocity = velocity.clone().subtract(Vector(0.0, GRAVITY, 0.0))
        val next = point.clone()
        val nextTravelled = travelled
        next.syncDelayed(1) { advanceSpit(player, next, nextVelocity, nextTravelled) }
    }

    private fun splash(world: World, at: Location) {
        IncursionArsenal.forced(world, Particle.SPLASH, at, 14, 0.15, 0.08)
        IncursionArsenal.forced(world, Particle.DUST, at, 10, 0.18, 0.0, SPIT_DUST)
        IncursionArsenal.forced(world, Particle.FALLING_WATER, at, 6, 0.15, 0.0)
        world.playSound(at, Sound.ENTITY_GENERIC_SPLASH, 0.35f, 1.6f)
    }
}
