package dev.lumas.lumaitems.items.weapons.scythe

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldKey
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.canDamage
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.extensions.toColor
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

@Disable(WorldKey.PINATA)
class DarksunScythe : CustomItemFunctions() {

    private companion object {
        const val TARGET_ENTITY_RANGE = 50
        const val TARGET_BLOCK_RANGE = 10

        const val HORIZONTAL_RADIUS = 2.0
        const val VERTICAL_RADIUS = 2.5
        const val DAMAGE = 14.0
        const val PULSE_COUNT = 7
        const val PULSE_INTERVAL_TICKS = 7
        const val COOLDOWN_TICKS = 8 * 20L

        const val SLASH_SPREAD = 1.5
        const val IMPACT_RING_POINTS = 24

        val KEY = "darksun-scythe".namespacedKey()
        val GRIM_SLOWNESS = PotionEffect(PotionEffectType.SLOWNESS, 56, 3, false, false, false)
        val DUST = ParticleDisplay.of(Particle.DUST)
        val COLORS = listOf("#574650", "#4b6270", "#C7817F", "#F2BC82", "#FCD56D")
            .map { it.toColor() }
    }

    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#574650:#4b6270:#C7817F:#F2BC82:#FCD56D>Darksun Scythe</gradient></b>")
        .customEnchants("<#D59580>Grim")
        .material(Material.NETHERITE_HOE)
        .persistentData(KEY)
        .tier(Tier.LUMARINE_2026)
        .lore(
            "<#D59580>Right-click</#D59580> to reap",
            "an area with seveeral",
            "waves of slashes.",
            "",
            "The first wave roots",
            "and slows down what",
            "it strikes.",
            "",
            "<red>Cooldown: 8s"
        )
        .vanillaEnchants(
            Enchantment.SHARPNESS to 9,
            Enchantment.BANE_OF_ARTHROPODS to 7,
            Enchantment.UNBREAKING to 10,
            Enchantment.LOOTING to 6,
            Enchantment.MENDING to 1
        )
        .buildPair()

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isOnCooldown(this) || !player.isItemInSlot(KEY, EquipmentSlot.HAND)) return

        val target = (player.getTargetEntity(TARGET_ENTITY_RANGE) as? LivingEntity)?.location
            ?: player.getTargetBlockExact(TARGET_BLOCK_RANGE)?.location?.add(0.5, 1.0, 0.5)
            ?: player.location.add(player.location.direction.multiply(TARGET_BLOCK_RANGE))

        player.addCooldown(this, COOLDOWN_TICKS)
        unleashGrim(player, target)
    }

    private fun unleashGrim(player: Player, center: Location) {
        val openingTargets = strike(player, center, isOpeningStrike = true)
        renderImpact(openingTargets, 0)

        var elapsed = 0
        var pulse = 1
        center.syncTimer(1, 1) { task ->
            elapsed++
            renderSlash(center, elapsed)

            if (elapsed % PULSE_INTERVAL_TICKS != 0) return@syncTimer

            val targets = strike(player, center, isOpeningStrike = false)
            renderImpact(targets, pulse++)

            if (pulse >= PULSE_COUNT) task.cancel()
        }
    }

    private fun strike(player: Player, center: Location, isOpeningStrike: Boolean): List<LivingEntity> {
        val targets = targetsAround(player, center).toList()
        targets.forEach { target ->
            target.damage(DAMAGE, player)

            if (isOpeningStrike) {
                target.velocity = Vector(0, 0, 0)
                target.addPotionEffect(GRIM_SLOWNESS)
            }
        }
        return targets
    }

    private fun targetsAround(player: Player, center: Location): Collection<LivingEntity> {
        return center.getNearbyLivingEntities(HORIZONTAL_RADIUS, VERTICAL_RADIUS, HORIZONTAL_RADIUS) { target ->
            target != player && target !is ArmorStand && player.canDamage(target)
        }
    }

    private fun renderSlash(center: Location, elapsed: Int) {
        val slash = center.clone().add(
            random.nextDouble(-SLASH_SPREAD, SLASH_SPREAD),
            random.nextDouble(0.0, 2.0),
            random.nextDouble(-SLASH_SPREAD, SLASH_SPREAD)
        )
        val color = elapsed / PULSE_INTERVAL_TICKS

        center.world.spawnParticle(Particle.SWEEP_ATTACK, slash, 1)
        pulseDust(color, 0.65f)
            .withCount(4)
            .offset(0.12)
            .spawn(slash)

        if (elapsed % 3 == 0) {
            center.world.spawnParticle(Particle.REVERSE_PORTAL, slash, 2, 0.08, 0.08, 0.08, 0.01)
        }

        center.world.playSound(center, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.23f)
    }

    private fun renderImpact(targets: Collection<LivingEntity>, pulse: Int) {
        targets.forEach { target ->
            val center = target.location.add(0.0, target.height * 0.5, 0.0)
            val radius = (target.width * 0.75 + 0.45).coerceIn(0.75, 1.8)
            val yaw = pulse * 0.73 + target.entityId * 0.19
            val pitch = Math.toRadians(28.0 + (pulse * 31 % 105))
            val roll = Math.toRadians(-52.0 + (pulse * 47 % 104))

            renderRing(center, radius, pulse * 0.29, yaw, pitch, roll, pulseDust(pulse, 0.9f))
            center.world.spawnParticle(Particle.REVERSE_PORTAL, center, 6, radius * 0.35, target.height * 0.2, radius * 0.35, 0.02)
        }
    }

    private fun renderRing(
        center: Location,
        radius: Double,
        rotation: Double,
        yaw: Double,
        pitch: Double,
        roll: Double,
        display: ParticleDisplay
    ) {
        repeat(IMPACT_RING_POINTS) { point ->
            val angle = rotation + Math.TAU * point / IMPACT_RING_POINTS
            val offset = Vector(cos(angle) * radius, 0.0, sin(angle) * radius)
                .rotateAroundX(pitch)
                .rotateAroundZ(roll)
                .rotateAroundY(yaw)

            display.spawn(center.clone().add(offset))
        }
    }

    private fun pulseDust(pulse: Int, size: Float): ParticleDisplay {
        val progress = pulse.coerceIn(0, PULSE_COUNT - 1).toDouble() / (PULSE_COUNT - 1)
        val color = (progress * COLORS.lastIndex).roundToInt()
        return DUST.clone().withColor(COLORS[color], size)
    }
}
