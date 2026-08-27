package dev.lumas.lumaitems.items.tools.nests

import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.toColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Tag
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.util.Vector
import java.awt.Color
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

abstract class AxiomMattock : CustomItemFunctions() {

    companion object {
        private const val RADIUS = 8.0
        private const val DURATION_TICKS = 200
        private const val COOLDOWN_TICKS = 180 * 20L
        private const val BEAM_RADIUS = 0.75
        private const val BEAMS_PER_TICK = 2
        private val MAX_BLOCK_HARDNESS = Material.OBSIDIAN.hardness
    }

    protected abstract val runeColor: Pair<Color, Color>
    protected abstract val shellColor: Pair<Color, Color>
    protected abstract val coreColor: Pair<Color, Color>
    protected abstract val loreColor: String

    protected abstract fun beamAxis(): Vector

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isOnCooldown(this)) return
        val targetBlock = player.getTargetBlockExact(25) ?: return
        player.addCooldown(this, COOLDOWN_TICKS)
        player.damageItemStack(EquipmentSlot.HAND, 50)

        val center = targetBlock.location.add(0.5, 1.0, 0.5)
        val runeDisplay = ParticleDisplay.of(Particle.DUST_COLOR_TRANSITION)
            .withTransitionColor(runeColor.first, runeColor.second, 1f)
            .withLocation(center)
        val shellDisplay = runeDisplay.clone()
            .withTransitionColor(shellColor.first, shellColor.second, 0.8f)
        val coreDisplay = ParticleDisplay.of(Particle.DUST_COLOR_TRANSITION)
            .withTransitionColor(coreColor.first, coreColor.second, 0.7f)
        val flareDisplay = ParticleDisplay.of(Particle.END_ROD)
            .withCount(1)
            .withExtra(0.0)

        var ticksRan = 0
        Executors.asyncTimer(0, 1) {
            val tick = ++ticksRan
            if (tick > DURATION_TICKS) {
                Particles.sphere(RADIUS, 12.0, shellDisplay.clone().withLocation(center))
                it.cancel()
                return@asyncTimer
            }
            if (tick == 1) {
                Particles.spikeSphere(1.0, 14.0, 70, 2.0, 5.0, coreDisplay.clone().withLocation(center))
            }

            val spin = tick * 0.06
            val runeRadius = RADIUS * (0.94 + sin(tick * 0.14) * 0.06)
            val star = runeDisplay.clone().rotate(0.0, spin, 0.0)
            val ring = shellDisplay.clone().rotate(0.0, spin, 0.0)
            Particles.neopaganPentagram(runeRadius, 0.06, 0.0, 0.5, 600.0, star, ring)
            Particles.polygon(5, 2, runeRadius * 0.45, 0.1, 0.0, shellDisplay.clone().rotate(0.0, -spin * 1.7, 0.0))

            val phi = tick * 0.11
            val ringY = cos(phi) * RADIUS
            Particles.circle(
                abs(sin(phi)) * RADIUS, 20.0,
                shellDisplay.clone().withLocation(center.clone().add(0.0, ringY, 0.0))
            )

            repeat(BEAMS_PER_TICK) { _ ->
                fireBeam(player, center, spin, coreDisplay, flareDisplay)
            }
        }
    }

    fun base(): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.NETHERITE_PICKAXE)
            .tier(Tier.LUMARINE_2026)
            .attributeModifiers(
                AttributeContainer.builder()
                    .setKey("axiom-mattock")
                    .setAttribute(Attribute.BLOCK_INTERACTION_RANGE)
                    .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                    .setSlot(EquipmentSlotGroup.HAND)
                    .setAmount(2.0)
                    .build()
            )
            .lore(
                "A special mattock that",
                "allows reaching farther",
                "when targeting blocks.",
                "",
                "<$loreColor>Right-click</$loreColor> to unleash",
                "a paradoxical beam that",
                "breaks nearby blocks in",
                "its enclosed radius.",
                "",
                "<red>Cooldown: 3m"
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 8,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.UNBREAKING to 6,
                Enchantment.MENDING to 1
            )
    }

    private fun fireBeam(
        player: Player,
        center: Location,
        spin: Double,
        coreDisplay: ParticleDisplay,
        flareDisplay: ParticleDisplay
    ) {
        val axis = beamAxis()
        val (perpA, perpB) = perpendicularBasis(axis)
        val angle = random().nextDouble(0.0, PI * 2)
        val distance = RADIUS * sqrt(random().nextDouble())
        val offset = perpA.clone().multiply(cos(angle) * distance)
            .add(perpB.clone().multiply(sin(angle) * distance))
        val halfLength = sqrt(RADIUS * RADIUS - distance * distance)

        val midpoint = center.clone().add(offset)
        val entry = midpoint.clone().subtract(axis.clone().multiply(halfLength))
        val exit = midpoint.clone().add(axis.clone().multiply(halfLength))

        BukkitVectors.line(entry, exit, 0.3)
            .map { loc -> loc.block }
            .distinct()
            .forEach { block ->
                block.sync {
                    val material = block.type
                    val hardness = material.hardness
                    if (!material.isAir &&
                        hardness >= 0.0f &&
                        hardness < MAX_BLOCK_HARDNESS &&
                        block.isTagged(Tag.MINEABLE_PICKAXE, Tag.MINEABLE_SHOVEL)
                    ) {
                        val brokenBlockData = block.blockData
                        block.breakNaturallyWithLog(player, player.inventory.itemInMainHand, false)
                        block.world.spawnParticle(Particle.BLOCK, block.location.add(0.5, 0.5, 0.5), 4, 0.3, 0.3, 0.3, brokenBlockData)
                    }
                }
            }

        Particles.line(entry, exit, 0.35, coreDisplay)
        val helix = coreDisplay.clone().withLocation(entry)
        val length = halfLength * 2
        var walked = 0.0
        while (walked <= length) {
            val twist = spin * 3 + walked * 0.9
            val along = axis.clone().multiply(walked)
            val swirl = perpA.clone().multiply(cos(twist) * BEAM_RADIUS)
                .add(perpB.clone().multiply(sin(twist) * BEAM_RADIUS))
            val first = along.clone().add(swirl)
            val second = along.clone().subtract(swirl)
            helix.spawn(first.x, first.y, first.z)
            helix.spawn(second.x, second.y, second.z)
            walked += 0.55
        }

        flareRing(entry, perpA, perpB, BEAM_RADIUS, flareDisplay)
        flareRing(exit, perpA, perpB, BEAM_RADIUS, flareDisplay)
    }

    private fun perpendicularBasis(axis: Vector): Pair<Vector, Vector> {
        val seed = if (abs(axis.y) > 0.9) BukkitVectors.EAST else BukkitVectors.UP
        val first = axis.clone().crossProduct(seed).normalize()
        return first to axis.clone().crossProduct(first).normalize()
    }

    private fun flareRing(location: Location, perpA: Vector, perpB: Vector, radius: Double, display: ParticleDisplay) {
        val ring = display.clone().withLocation(location)
        var theta = 0.0
        while (theta < PI * 2) {
            val point = perpA.clone().multiply(cos(theta) * radius)
                .add(perpB.clone().multiply(sin(theta) * radius))
            ring.spawn(point.x, point.y, point.z)
            theta += PI / 6
        }
    }
}

class UnionAxiomMattockItem : AxiomMattock() {

    override val runeColor = "#954381".toColor() to "#ED70BB".toColor()
    override val shellColor = "#ED70BB".toColor() to "#954381".toColor()
    override val coreColor = "#FFD9F0".toColor() to "#ED70BB".toColor()
    override val loreColor = "#ED70BB"

    override fun beamAxis(): Vector = BukkitVectors.UP

    override fun createItem() = base()
        .name("<b><gradient:#D8B9FB:#C08EFF:#E07DFF>Axiom Mattock</gradient></b>")
        .customEnchants("<gray>Reach II", "<#ED70BB>Union")
        .persistentData("union-paradox-mattock")
        .buildPair()
}

class FluxAxiomMattockItem : AxiomMattock() {

    override val runeColor = "#3B3FA8".toColor() to "#5FE8FF".toColor()
    override val shellColor = "#5FE8FF".toColor() to "#3B3FA8".toColor()
    override val coreColor = "#DDF7FF".toColor() to "#5FE8FF".toColor()
    override val loreColor = "#5FE8FF"

    override fun beamAxis(): Vector  {
        val y = random().nextDouble(-1.0, 1.0)
        val theta = random().nextDouble(0.0, PI * 2)
        val radius = sqrt(1 - y * y)
        return Vector(cos(theta) * radius, y, sin(theta) * radius)
    }

    override fun createItem() = base()
        .name("<b><gradient:#D8B9FB:#AC8EFF:#7D9AFF>Axiom Mattock</gradient></b>")
        .customEnchants("<gray>Reach II", "<#5FE8FF>Flux")
        .persistentData("flux-paradox-mattock")
        .buildPair()
}
