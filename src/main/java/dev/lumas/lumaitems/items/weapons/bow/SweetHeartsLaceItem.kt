package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import kotlin.random.Random
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Enemy
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class SweetHeartsLaceItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fb5ab6&lS&#fb5db5&lw&#fb61b4&le&#fb64b3&le&#fc68b1&lt&#fc6bb0&lH&#fc6eaf&le&#fc72ae&la&#fc75ad&lr&#fc79ac&lt&#fc7cab&l'&#fc7faa&ls &#fd83a8&lL&#fd86a7&la&#fd8aa6&lc&#fd8da5&le",
            mutableListOf("&#FB5AB6Cupid"),
            mutableListOf("Shot enemies may be briefly", "charmed, causing them to", "shortly become passive"),
            Material.BOW,
            mutableListOf("sweetheartslace"),
            mutableMapOf(
                Enchantment.POWER to 7,
                Enchantment.KNOCKBACK to 2,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1,
                Enchantment.LOOTING to 4
            )
        )
        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"

        return Pair("sweetheartslace", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.PROJECTILE_LAUNCH -> {
                event as ProjectileLaunchEvent
                val nearbyWatchers = player.getNearbyEntities(50.0, 50.0, 50.0).mapNotNull { it as? Player }

                val snowball: Snowball = event.entity.location.world.spawn(event.entity.location, Snowball::class.java)
                snowball.velocity = event.entity.velocity
                event.entity.remove() // Can remove after we pull its location and velocity
                snowball.setGravity(false)

                player.hideEntity(instance(), snowball)
                for (watcher in nearbyWatchers) {
                    watcher.hideEntity(instance(), snowball)
                }
                snowball.persistentDataContainer.set(NamespacedKey(instance(), "sweetheartslace"), PersistentDataType.SHORT, 1)
                snowball.shooter = player

                snowball.syncTimer(0, 1) {
                    if (snowball.isDead || snowball.ticksLived > 200) {
                        it.cancel()
                        if (!snowball.isDead) snowball.remove()
                        return@syncTimer
                    }
                    snowball.world.spawnParticle(Particle.HEART, snowball.location, 2, 0.3, 0.2, 0.3, 0.3)
                }

            }
            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent
                val entity = event.hitEntity as? LivingEntity ?: return false

                entity.world.spawnParticle(Particle.WITCH, entity.location, 30, 0.5, 0.5, 0.5, 0.5)

                if (entity is Enemy && Random.Default.nextInt(100) <= 40) {
                    entity.persistentDataContainer.set(NamespacedKey(instance(), "sweetheartslace"), PersistentDataType.SHORT, 1.toShort())
                    entity.syncDelayed(600) {
                        if (entity.isDead) return@syncDelayed
                        entity.persistentDataContainer.remove(NamespacedKey(instance(), "sweetheartslace"))
                    }
                }
            }
            Action.ENTITY_TARGET_PLAYER -> {
                event as EntityTargetLivingEntityEvent
                if (!event.entity.persistentDataContainer.has(NamespacedKey(instance(), "sweetheartslace"))) return false
                event.isCancelled = true
            }
            Action.ENTITY_DAMAGE -> {
                event as EntityDamageByEntityEvent
                event.damage += 12.0
            }

            else -> return false
        }
        return true
    }

}