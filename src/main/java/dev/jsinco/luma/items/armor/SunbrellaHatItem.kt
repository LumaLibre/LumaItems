package dev.jsinco.luma.items.armor

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector
import java.util.concurrent.ConcurrentHashMap

class SunbrellaHatItem : CustomItem {

    // Usage:
    // - Summer 2024
    // Inspiration:
    // - Summer sun hats
    // - VALORANT Jett
    // Idea:
    // Movement-based/utility helmet
    // - Ability to glide down
    // - Ability to jump higher
    // - Ability to knockback enemies further
    // - Slide forward?

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
        private val dustOption = DustOptions(Color.WHITE, 1f)
        private val fallingPlayers: ConcurrentHashMap<Player, Double> = ConcurrentHashMap()
    }


    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#A0F562&lS&#B6DD75&lu&#CCC487&ln&#E2AC9A&lb&#F893AC&lr&#E5A2BE&le&#D1B0D1&ll&#BEBFE3&ll&#AACDF5&la&#ABC1F3&l &#ABB6F2&lH&#ACAAF0&la&#AC9EEE&lt",
            mutableListOf("&#B4E591Breezy Day"),
            mutableListOf("&#F893ACTailwind &7- &fWhen falling, shift to glide", "down and mitigate fall damage.", "",
                "&#AACDF5Trailblazer &7- &fWhile wearing, knockback", "attacks will launch enemies further", "and faster.", "",
                "&#AC9EEEWindTye &7- &fWhile wearing, arrows", "fired will be converted to wind", "slashes that glide through the air."),
            Material.NETHERITE_HELMET,
            mutableListOf("sunbrellahat"),
            mutableMapOf(Enchantment.PROTECTION to 5, Enchantment.FEATHER_FALLING to 6, Enchantment.PROJECTILE_PROTECTION to 4, Enchantment.UNBREAKING to 4, Enchantment.MENDING to 1)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        return Pair("sunbrellahat", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.PLAYER_CROUCH -> {
                if (!player.isSneaking && player.velocity.y < -0.1) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
                        fallingPlayers[player] = 0.77
                    }, 200L)
                }
            }

            Action.ASYNC_RUNNABLE -> {
                for (fallingPlayer in fallingPlayers.keys) {
                    if (!isGliding(fallingPlayer)) {
                        fallingPlayers.remove(fallingPlayer)
                    }
                }
            }

            Action.MOVE -> {
                if (!player.isSneaking || player.velocity.y > -0.1) {
                    return false
                }
                val blockIterator = BlockIterator(player.world, player.location.toVector(), Vector(0.0, -1.0, 0.0), 0.0, 2)
                while (blockIterator.hasNext()) {
                    if (!blockIterator.next().type.isAir) {
                        return false
                    }
                }


                val multiplier = fallingPlayers[player] ?: 0.5

                // Good enough I guess. I'd prefer the player to glide in the direction they're actually moving, but I'm not sure how to do that
                val vec = player.location.direction.multiply(0.25)
                player.velocity = Vector(vec.x, player.velocity.y * multiplier, vec.z)
                player.fallDistance = 0.0f // Update this when the player actually changes blocks or keep this?
                player.world.spawnParticle(Particle.DUST, player.location, 4, 0.3, 0.0, 0.3, dustOption)
            }

            Action.ENTITY_DAMAGE -> {
                event as EntityDamageByEntityEvent
                val livingEntity = event.entity as? LivingEntity ?: return false

                // Check for knockback attack - TODO: Is there a DamageSource for this?
                if (!event.isCritical && player.isSprinting) {
                    // Adjust velocity of the attacked entity to knock them back further than normal.
                    val vector: Vector = livingEntity.location.toVector().subtract(player.location.toVector()).multiply(1.5).add(Vector(0.0, 0.1, 0.0))

                    vector.x = (-3.0).coerceAtLeast(vector.x.coerceAtMost(3.0))
                    vector.y = (-3.0).coerceAtLeast(vector.y.coerceAtMost(3.0))
                    vector.z = (-3.0).coerceAtLeast(vector.z.coerceAtMost(3.0))
                    livingEntity.velocity = vector
                    livingEntity.world.spawnParticle(Particle.DUST, livingEntity.location, 10, 0.5, 0.5, 0.5, dustOption)
                }
            }

            Action.PROJECTILE_LAUNCH -> {
                event as ProjectileLaunchEvent
                val projectile = event.entity as? Arrow ?: return false
                projectile.setGravity(false)
                projectile.isPersistent = false
                projectile.persistentDataContainer.set(NamespacedKey(plugin, "sunbrellahat"), PersistentDataType.SHORT, 0)
                player.hideEntity(plugin, projectile)
                for (entity in projectile.getNearbyEntities(100.0, 100.0, 100.0)) {
                    if (entity is Player) {
                        entity.hideEntity(plugin, projectile)
                    }
                }


                object : BukkitRunnable() {

                    var count = 0

                    override fun run() {
                        if (projectile.isDead) {
                            this.cancel()
                            return
                        }

                        projectile.world.spawnParticle(Particle.SWEEP_ATTACK, projectile.location, 1, 0.0, 0.0, 0.0, 0.0)
                        projectile.world.spawnParticle(Particle.CLOUD, projectile.location, 1, 0.1, 0.1, 0.1, 0.0)


                        if (count >= 130) {
                            projectile.remove()
                            this.cancel()
                            return
                        }
                        count+= 3
                    }
                }.runTaskTimer(plugin, 1L, 3L)
            }

            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent
                event.entity.remove()
            }

            else -> return false
        }
        return true
    }


    private fun isGliding(player: Player): Boolean {
        return player.isSneaking && player.velocity.y > -0.01
    }

}