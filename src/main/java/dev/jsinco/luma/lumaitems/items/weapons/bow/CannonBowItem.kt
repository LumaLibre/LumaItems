package dev.jsinco.luma.lumaitems.items.weapons.bow

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.manager.GlowManager
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent
import org.bukkit.ChatColor
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class CannonBowItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#090909&lC&#1b1911&la&#2e281a&ln&#403822&ln&#52472b&lo&#645733&ln&#77663b&lB&#897644&lo&#9b854c&lw",
            mutableListOf("&#7E7E7ECannonball"),
            mutableListOf("§fA medieval invention, now in","§fyour hands!","","§fWith an empty offhand, fire a cannonball","§fprojectile that will explode on impact","","§c1 TNT per shot"),
            Material.CROSSBOW,
            mutableListOf("cannonbow"),
            mutableMapOf(Enchantment.QUICK_CHARGE to(3), Enchantment.UNBREAKING to(5), Enchantment.MENDING to(1), Enchantment.INFINITY to(1), Enchantment.MULTISHOT to(1))
        )
        return Pair("cannonbow", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val crossbowLoad: EntityLoadCrossbowEvent? = event as? EntityLoadCrossbowEvent
        val projectileLaunch: ProjectileLaunchEvent? = event as? ProjectileLaunchEvent
        val projectileLand: ProjectileHitEvent? = event as? ProjectileHitEvent

        when (type) {
            Action.CROSSBOW_LOAD -> {
                crossbowLoad?.isCancelled = cannonBallLoad(player)
            }
            Action.PROJECTILE_LAUNCH -> {
                cannonBallLaunch(projectileLaunch!!.entity, player)
            }
            Action.PROJECTILE_LAND -> {
                cannonBallLand(projectileLand!!.entity)
            }

            else -> return false
        }
        return true
    }

    private fun cannonBallLoad(p: Player): Boolean {
        if (p.gameMode == GameMode.CREATIVE) return false
        return if (p.inventory.contains(Material.TNT)) {
            p.inventory.removeItem(ItemStack(Material.TNT, 1))
            p.inventory.addItem(ItemStack(Material.ARROW, 1))
            false
        } else {
            true
        }
    }

    private fun cannonBallLaunch(projectile: Projectile, p: Player) {
        if (!p.inventory.itemInOffHand.type.isAir) {
            p.sendMessage("§cYou can't use this item while holding something in your offhand!")
            return
        }

        val cannonBall = p.world.spawn(projectile.location, EnderPearl::class.java)
        GlowManager.setGlowColor(cannonBall, ChatColor.BLACK)
        cannonBall.isGlowing = true
        cannonBall.persistentDataContainer.set(NamespacedKey(instance(), "cannonbow"), PersistentDataType.SHORT, 1)
        cannonBall.velocity = projectile.velocity
    }

    private fun cannonBallLand(projectile: Projectile) {
        projectile.world.createExplosion(projectile.location, 6f, false, false)
    }
}