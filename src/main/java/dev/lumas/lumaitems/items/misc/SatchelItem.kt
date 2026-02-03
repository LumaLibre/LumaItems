package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.manager.GlowManager
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.Executors.syncEntityDelayed
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.function.Consumer

class SatchelItem : CustomItem {

    private lateinit var satchel: ItemFactory


    override fun createItem(): Pair<String, ItemStack> {
        satchel = ItemFactory(
            "&#7b5aff&lS&#8769ff&la&#9277ff&lt&#9e86ff&lc&#857afe&lh&#6b6ffd&le&#5263fc&ll",
            mutableListOf("&#5263fcL&#5263fca&#5263fcu&#5263fcn&#5263fcc&#5263fch"),
            mutableListOf("§fUpon landing, this item will explode into", "§fparticles and launch nearby entities","","§fWhen falling, launches are instant"),
            Material.SNOWBALL,
            mutableListOf("satchel"),
            mutableMapOf(Enchantment.INFINITY to 1)
        )
        return Pair("satchel", satchel.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val projectileLaunch: ProjectileLaunchEvent? = event as? ProjectileLaunchEvent
        val projectileHit: ProjectileHitEvent? = event as? ProjectileHitEvent

        when (type) {
            Action.PROJECTILE_LAUNCH -> {
                satchelLaunch(projectileLaunch!!.entity, player)
            }
            Action.PROJECTILE_LAND -> {
                satchelDetonate(projectileHit!!.entity, player)
            }
            else -> return false
        }
        return true
    }


    // satchel has been launched, set glow and itemdata to brace for hitevent, if player is falling satchel should launch after 5 ticks
    private fun satchelLaunch(projectile: Entity, p: Player) {
        projectile.persistentDataContainer.set(NamespacedKey(instance(), "satchel"), PersistentDataType.SHORT, 1)
        GlowManager.setGlowColor(projectile, GlowManager.COLORS.random())

        if (!p.location.subtract(0.0, 0.1, 0.0).block.type.isAir || p.isFlying) return

        projectile.syncEntityDelayed(5) {
            if (!projectile.isDead) {
                satchelDetonate(projectile, p)
            }
        }
    }

    private fun satchelDetonate(satchelEntity: Entity, p: Player) {
        val nearbyEntities: Collection<Entity> = satchelEntity.getNearbyEntities(3.0, 6.5, 3.0)
        nearbyEntities.forEach(Consumer { entity: Entity ->
            if (AbilityUtil.noDamagePermission(p, entity)) return@Consumer
            entity.velocity = Vector(
                entity.facing.getDirection().x * 2,
                1.0,
                entity.facing.getDirection().z * 2
            )
            entity.world.spawnParticle(Particle.GLOW, entity.location, 30, 0.5, 0.5, 0.5, 0.1)
        })
        p.world.playSound(p.location, Sound.ENTITY_ALLAY_AMBIENT_WITH_ITEM, 1f, 1f)
        satchelEntity.world.spawnParticle(Particle.GLOW, satchelEntity.location, 10)
        satchelEntity.remove()
        if (p.gameMode != GameMode.CREATIVE) p.inventory.addItem(satchel.createItem())
    }
}