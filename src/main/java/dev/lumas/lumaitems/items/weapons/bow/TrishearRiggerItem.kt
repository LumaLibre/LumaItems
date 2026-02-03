package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.enums.CardinalDirection
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.manager.GlowManager
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.syncDelayed
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import java.awt.Color
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class TrishearRiggerItem : CustomItemFunctions() {

    companion object {
        private val nameSpace = Util.namespacedKey("trishear-rigger")
        private val typeNameSpace = Util.namespacedKey("trishear-rigger-type")
    }

    private enum class ArrowCloneType(val color: Color, val glowColor: NamedTextColor, val block: (originalArrow: Arrow, clonedArrow: Arrow) -> Unit) {
        LEFT(Util.hex2AwtColor("#f2dc59"), NamedTextColor.RED, { originalArrow, clonedArrow ->
            // shift it's direction to the left just a little bit
            val originalDir = originalArrow.velocity.clone().normalize()
            val speed = originalArrow.velocity.length()
            val leftDir = BukkitVectors.rotateVectorY(originalDir, Math.toRadians(-5.0))

            clonedArrow.velocity = leftDir.multiply(speed)
        }),
        RIGHT(Util.hex2AwtColor("#f1b46c"), NamedTextColor.RED, { originalArrow, clonedArrow ->
            val originalDir = originalArrow.velocity.clone().normalize()
            val speed = originalArrow.velocity.length()
            val leftDir = BukkitVectors.rotateVectorY(originalDir, Math.toRadians(5.0))

            clonedArrow.velocity = leftDir.multiply(speed)
        }),
        UP(Util.hex2AwtColor("#e58c7a"), NamedTextColor.RED, { originalArrow, clonedArrow ->
            val originalDir = originalArrow.velocity.clone().normalize()
            val speed = originalArrow.velocity.length()

            val cardinalDirection = CardinalDirection.fromEntityYaw(originalArrow)
            val angle = Math.toRadians(-5.0) // negative angle = up

            val upDir = if (cardinalDirection == CardinalDirection.NORTH || cardinalDirection == CardinalDirection.SOUTH) {
                BukkitVectors.rotateVectorX(originalDir, angle)
            } else {
                BukkitVectors.rotateVectorZ(originalDir, angle)
            }

            clonedArrow.velocity = upDir.multiply(speed)
        }),
        DOWN(Util.hex2AwtColor("#bb6898"), NamedTextColor.RED, { originalArrow, clonedArrow ->
            val originalDir = originalArrow.velocity.clone().normalize()
            val speed = originalArrow.velocity.length()
            val angle = Math.toRadians(5.0) // positive angle = down

            val cardinalDirection = CardinalDirection.fromEntityYaw(originalArrow)
            val downDir = if (cardinalDirection == CardinalDirection.NORTH || cardinalDirection == CardinalDirection.SOUTH) {
                BukkitVectors.rotateVectorX(originalDir, angle)
            } else {
                BukkitVectors.rotateVectorZ(originalDir, angle)
            }

            clonedArrow.velocity = downDir.multiply(speed)
        }),
        CENTER(Util.hex2AwtColor("#90669e"), NamedTextColor.DARK_PURPLE, { _, _ ->
            throw IllegalStateException("Center clone type should not be used for cloning arrows.")
        })
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#f2dc59:#f1b46c:#e58c7a:#bb6898:#90669e>Trishear Rigger</gradient></b>")
            .customEnchants("<#90669e>Triple Strike")
            .persistentData(nameSpace)
            .material(Material.BOW)
            .tier(Tier.SUMMER_2025)
            .vanillaEnchants(
                Enchantment.PIERCING to 3,
                Enchantment.POWER to 4,
                Enchantment.UNBREAKING to 5,
                Enchantment.MENDING to 1,
                Enchantment.LOOTING to 4
            )
            .lore(
                "This bow fires <#90669e>3</#90669e> arrows at",
                "a time, each with a different",
                "trajectory.",
                "",
                "Jump to adjust the heading",
                "of the arrows fired.",
                "",
                "Upon a full draw, arrows will",
                "explode after a short delay",
                "on impact.",
            )
            .buildPair()
    }

    override fun onPlayerShootBow(player: Player, event: EntityShootBowEvent) {
        val arrow = event.projectile as? Arrow ?: return
        if (Util.hasPersistentKey(arrow, nameSpace)) return // already cloned

        val shouldExplode = event.force >= 0.8f
        arrow.isCritical = false
        Util.setPersistentKey(arrow, nameSpace, PersistentDataType.BOOLEAN, shouldExplode)
        Util.setPersistentKey(arrow, typeNameSpace, PersistentDataType.STRING, ArrowCloneType.CENTER.name)

        val (cloneType1, cloneType2) = when {
            AbilityUtil.isOnGround(player) || (player.isFlying && !player.isJumping) ->
                ArrowCloneType.LEFT to ArrowCloneType.RIGHT
            else -> ArrowCloneType.UP to ArrowCloneType.DOWN
        }


        val arrows = mapOf(
            ArrowCloneType.CENTER to arrow,
            cloneArrow(arrow, player, shouldExplode, cloneType1),
            cloneArrow(arrow, player, shouldExplode, cloneType2)
        )
        val particleDisplay = ParticleDisplay.of(Particle.DUST)
        var count = 0

        Executors.asyncTimer(0, 1) { task ->
            if (count++ >= 400 || arrows.all { val e = it.value; e.isInBlock || e.isDead }) {
                task.cancel()
                return@asyncTimer
            }
            arrows.forEach {
                particleDisplay.withColor(it.key.color).spawn(it.value.location)
            }
        }
    }


    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        event.hitEntity?.let { hitEntity ->
            if (hitEntity is LivingEntity) {
                hitEntity.noDamageTicks = 0 // reset no damage ticks to allow for immediate damage
            }
            return
        }

        if (event.hitBlock == null) return // no block hit, nothing to do

        val projectile = event.entity
        val shouldExplode = Util.getPersistentKey(projectile, nameSpace, PersistentDataType.BOOLEAN) ?: false

        if (!shouldExplode) return

        val type = Util.getPersistentKey(projectile, typeNameSpace, PersistentDataType.STRING)
            ?.let { Util.enumValueOfOrNull(ArrowCloneType::class.java, it) } ?: ArrowCloneType.CENTER


        GlowManager.setGlowColor(projectile, type.glowColor)
        projectile.isGlowing = true


        projectile.syncDelayed(20) {
            val particleDisplay = ParticleDisplay.of(Particle.DUST)
                .withColor(type.color).withLocation(projectile.location)
            Particles.spikeSphere(1.0, 10.0, 3, 0.1, 0.6, particleDisplay)
            projectile.location.getNearbyLivingEntities(2.5).forEach {
                // Damage based on distance, with a max damage of 6.0
                if (it != player) {
                    val distance = it.location.distance(projectile.location)
                    val damage = (6.0 - distance).coerceAtLeast(0.0).coerceAtMost(6.0)
                    it.damage(damage, player)
                }
            }
            projectile.world.playSound(projectile.location, Sound.ENTITY_GENERIC_EXPLODE, 0.2f, 4.1f)
            projectile.remove()
        }
    }

    private fun cloneArrow(originalArrow: Arrow, player: Player, shouldExplode: Boolean, type: ArrowCloneType): Pair<ArrowCloneType, Arrow> {
        val clonedArrow = player.world.createEntity(
            originalArrow.location,
            Arrow::class.java
        )
        type.block(originalArrow, clonedArrow)
        clonedArrow.isCritical = originalArrow.isCritical
        clonedArrow.shooter = player
        clonedArrow.pierceLevel = originalArrow.pierceLevel
        clonedArrow.color = originalArrow.color
        originalArrow.customEffects.forEach { effect ->
            clonedArrow.addCustomEffect(effect, true)
        }
        clonedArrow.pickupStatus = AbstractArrow.PickupStatus.CREATIVE_ONLY

        Util.setPersistentKey(clonedArrow, nameSpace, PersistentDataType.BOOLEAN, shouldExplode)
        Util.setPersistentKey(clonedArrow, typeNameSpace, PersistentDataType.STRING, type.name)
        clonedArrow.spawnAt(originalArrow.location)
        return type to clonedArrow
    }

}
