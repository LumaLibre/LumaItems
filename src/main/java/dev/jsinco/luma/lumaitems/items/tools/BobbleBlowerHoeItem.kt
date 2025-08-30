package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.enums.BlockConstants
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.BukkitVectors
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.QuickTasks
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import java.awt.Color
import org.bukkit.FluidCollisionMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector


class BobbleBlowerHoeItem : CustomItemFunctions() {

    companion object {
        private val DYE_ITEM = ItemStack(Material.LIGHT_BLUE_DYE).apply {
            addUnsafeEnchantment(Enchantment.UNBREAKING, 1)
        }
        private val PARTICLE_DISPLAY = ParticleDisplay.of(Particle.DUST).withColor(Color.decode("#c5dcee"))
        private val HOLDER_HOE_ITEM = ItemStack(Material.NETHERITE_HOE).apply {
            addUnsafeEnchantment(Enchantment.FORTUNE, 5)
        }
    }

    private val nameSpace = Util.namespacedKey("bobble-blower")
    private val cloneSnowball = fun(snowball: Snowball, newVelocity: Vector): Snowball {
        val newSnowball = snowball.world.createEntity(snowball.location, Snowball::class.java)
        newSnowball.velocity = newVelocity
        newSnowball.shooter = snowball.shooter
        newSnowball.item = snowball.item
        newSnowball.setGravity(snowball.hasGravity())
        newSnowball.isPersistent = false
        Util.setPersistentKey(newSnowball, nameSpace, PersistentDataType.SHORT, 1)
        return newSnowball
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#f9d3ef:#dec1ee:#c5dcee:#f6c5c5:#EDEE9A>Bobble Blower Hoe</gradient></b>")
            .customEnchants("<#dec1ee>Bubble Lobber")
            .material(Material.NETHERITE_HOE)
            .persistentData(nameSpace)
            .tier(Tier.SUMMER_2025)
            .attributeModifiers(
                DefaultAttributes.NETHERITE_HOE.appendThenGetAttributes(
                    AttributeContainer.of(nameSpace, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, -2.4, EquipmentSlotGroup.HAND)
                )
            )
            .lore(
                "A fancy hoe with a bubble",
                "loop attached to it.",
                "",
                "<#dec1ee>Left-click</#dec1ee> to lob bubbles",
                "that will bounce in the",
                "direction you are facing.",
                "",
                "Crops in the way will be",
                "destroyed by bubbles.",
                "",
                "<red>Cooldown: 5s"
            )
            .vanillaEnchants(
                Enchantment.FORTUNE to 5,
                Enchantment.MENDING to 1,
                Enchantment.EFFICIENCY to 6,
                Enchantment.UNBREAKING to 7
            )
            .buildPair()
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        if (player.attackCooldown != 1.0f || QuickTasks.isOnCooldown(this, player)) return
        QuickTasks.addCooldown(this, player, 100)

        val snowballs = mutableListOf<Snowball>()

        val snowball = player.launchProjectile(Snowball::class.java).also { snowballs.add(it) }
        snowball.velocity = snowball.velocity.multiply(0.35)
        snowball.isPersistent = false
        snowball.item = DYE_ITEM
        Util.setPersistentKey(snowball, nameSpace, PersistentDataType.SHORT, 1)

        snowballs.add(extraSnowball(snowball, -6.0))
        snowballs.add(extraSnowball(snowball, 6.0))

        trackSnowball(player, snowballs)
        snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.0f, 1.0f)
    }


    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return
        val hitBlockFace = event.hitBlockFace ?: return

        if (snowball.location.distance(player.location) > 20 || snowball.velocity.length() <= 0.2) {
            snowball.remove()
            return
        }

        val vector = BukkitVectors.bounceWithBlockFace(snowball, hitBlockFace, 0.85)

        val newSnowball = cloneSnowball(snowball, vector)
        newSnowball.spawnAt(snowball.location)
        trackSnowball(player, newSnowball)

        snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.0f, 1.0f)
    }


    private fun extraSnowball(initialSnowball: Snowball, yOffset: Double): Snowball {
        val originalDir = initialSnowball.velocity.clone().normalize()
        val speed = initialSnowball.velocity.length()
        val newDir = BukkitVectors.rotateVectorY(originalDir, Math.toRadians(yOffset))

        val newSnowball = cloneSnowball(initialSnowball, newDir.multiply(speed))
        newSnowball.spawnAt(initialSnowball.location)
        return newSnowball
    }

    private fun processSnowballRaytrace(player: Player, snowball: Snowball) {
        // vfx
        PARTICLE_DISPLAY.spawn(snowball.location)


        val raytrace = snowball.world.rayTraceBlocks(
            snowball.location.add(0.0, 1.0, 0.0),
            BukkitVectors.DOWN,
            7.0,
            FluidCollisionMode.NEVER,
            false
        ) { b -> BlockConstants.CROPS.contains(b.type) }
        val hitBlock = raytrace?.hitBlock ?: return
        if (!AbilityUtil.noBuildPermission(player, hitBlock)) {
            val item = player.inventory.itemInMainHand
            item.damage(1, player) // no direct reference for item damaging.
            hitBlock.breakNaturally(HOLDER_HOE_ITEM)
        }
    }

    private fun trackSnowball(player: Player, snowballs: Collection<Snowball>) {
        Executors.syncTimer(0, 2) { task ->
            if (snowballs.all { it.isDead || it.location.block.isLiquid }) {
                task.cancel()
                return@syncTimer
            }
            for (s in snowballs) {
                if (s.isDead) continue
                processSnowballRaytrace(player, s)
            }
        }
    }

    private fun trackSnowball(player: Player, snowball: Snowball) {
        Executors.syncTimer(0, 2) { task ->
            if (snowball.isDead || snowball.location.block.isLiquid) {
                task.cancel()
                if (!snowball.isDead) {
                    snowball.remove()
                }
                return@syncTimer
            }
            processSnowballRaytrace(player, snowball)
        }
    }
}