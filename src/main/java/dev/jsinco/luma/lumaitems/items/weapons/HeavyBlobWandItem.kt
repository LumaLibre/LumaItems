package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.BukkitVectors
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import java.awt.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector


class HeavyBlobWandItem : CustomItemFunctions() {

    companion object {
        private val nameSpace = Util.namespacedKey("heavy-blob-wand")
        // light blue "#A2BFFE"
        private val colors: Map<ItemStack, ParticleDisplay> = mapOf(
            Material.PURPLE_DYE to "#C9A0DC",
            Material.MAGENTA_DYE to "#F49AC2",
            Material.PINK_DYE to "#FFC5D3",
            Material.LIGHT_BLUE_DYE to "#A2BFFE",
        )
            .mapKeys { ItemStack(it.key).apply { addUnsafeEnchantment(Enchantment.UNBREAKING, 1) } }
            .mapValues { ParticleDisplay.of(Particle.DUST).withColor(Color.decode(it.value)) }
    }



    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#C9A0DC:#F49AC2:#FFC5D3:#A2BFFE>Heavy Blob Wand</gradient></b>")
            .customEnchants("<#C9A0DC>Bubble Stream")
            .material(Material.NETHERITE_SWORD)
            .persistentData(nameSpace)
            .attributeModifiers(
                DefaultAttributes.NETHERITE_SWORD.appendThenGetAttributes(
                    AttributeContainer.of(nameSpace, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, -3.35, EquipmentSlotGroup.HAND)
                )
            )
            .tier(Tier.SUMMER_2025)
            .lore(
                "A sharp wand capable of",
                "lobbing deadly bubbles.",
                "Welcome to the future of",
                "bubble blowing.",
                "",
                "<#C9A0DC>Left-click</#C9A0DC> to lob bubbles",
                "in your faced direction.",
                "",
                "Bubbles will bounce off",
                "surfaces and deal critical",
                "damage to hit entities.",
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 10,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1,
                Enchantment.KNOCKBACK to 1,
                Enchantment.LOOTING to 5,
                Enchantment.SWEEPING_EDGE to 9
            )
            .buildPair()
    }


    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        val cooldown: Double = player.attackCooldown.let {
            if (it >= 0.9f) 1.0 else it.toDouble()
        }
        if (cooldown < 0.45) return

        val bubblesToSpawn = (cooldown / 0.25).toInt()
        val keys = colors.keys.toList()
        var spawned = 0
        Executors.syncTimer(0, 2) { task ->
            if (spawned++ >= bubblesToSpawn) {
                task.cancel()
                return@syncTimer
            }
            val direction = player.eyeLocation.direction
            val launchLoc = player.eyeLocation.add(direction.normalize().multiply(0.5))
            val snowball = player.world.createEntity(launchLoc, Snowball::class.java)
            snowball.shooter = player
            snowball.velocity = direction.multiply(1.09)
            snowball.isPersistent = false
            snowball.item = keys[(spawned - 1) % keys.size]
            Util.setPersistentKey(snowball, nameSpace, PersistentDataType.SHORT, 1)
            snowball.spawnAt(launchLoc)

            snowballRepeatExecutor(snowball)
            snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.0f, 1.0f)
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        (event.hitEntity as? LivingEntity)?.let {
            if (AbilityUtil.noDamagePermission(player, it)) return

            it.damage(13.0, player)
            it.velocity = BukkitVectors.ZERO
            it.noDamageTicks = 0
            val item = player.inventory.itemInMainHand
            item.damage(1, player)
            return
        }

        val snowball = event.entity as? Snowball ?: return
        val hitBlockFace = event.hitBlockFace ?: return
        if (snowball.location.distance(player.location) > 20 || snowball.velocity.length() <= 0.3) {
            snowball.remove()
            return
        }

        val vector = BukkitVectors.bounceWithBlockFace(snowball, hitBlockFace, 0.88)
        snowballRepeatExecutor(cloneSnowball(snowball, vector))

        snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.0f, 1.0f)
    }


    private fun cloneSnowball(snowball: Snowball, vector: Vector): Snowball {
        return cloneSnowball(snowball, snowball.location, vector)
    }
    private fun cloneSnowball(snowball: Snowball, newOrigin: Location, newVelocity: Vector): Snowball {
        val newSnowball = snowball.world.createEntity(newOrigin, Snowball::class.java)
        newSnowball.velocity = newVelocity
        newSnowball.shooter = snowball.shooter
        newSnowball.item = snowball.item
        newSnowball.setGravity(snowball.hasGravity())
        newSnowball.isPersistent = false
        Util.setPersistentKey(newSnowball, nameSpace, PersistentDataType.SHORT, 1)
        newSnowball.spawnAt(newOrigin)
        return newSnowball
    }

    private fun snowballRepeatExecutor(snowball: Snowball) {
        Executors.asyncTimer(0, 2) { task ->
            if (snowball.isDead || snowball.location.block.isLiquid) {
                task.cancel()
                if (!snowball.isDead) {
                    sync { snowball.remove() }
                }
                return@asyncTimer
            }
            val particleDisplay = colors[snowball.item] ?: return@asyncTimer
            particleDisplay.spawn(snowball.location)
        }
    }
}