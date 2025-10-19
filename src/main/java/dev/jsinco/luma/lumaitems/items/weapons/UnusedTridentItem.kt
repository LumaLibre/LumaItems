package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.entity.Trident
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class UnusedTridentItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("unused trident")
            .persistentData("unused-trident")
            .material(Material.TRIDENT)
            .buildPair()
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        Util.setPersistentKey(event.entity, "unused-trident", PersistentDataType.SHORT, 1)
    }


    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val trident = event.entity as? Trident ?: return
        if (player.isFlying) {
            player.isFlying = false
        }
        flyToProjectile(trident, player)
    }

    private fun flyToProjectile(trident: Trident, player: Player) {
        val i = 9
        Executors.syncTimer(0, 1) {

            val tridentPos = trident.location.toVector()
            val eyePos = player.eyeLocation.toVector()
            val vecToTrident = tridentPos.subtract(eyePos) // direction from player -> trident

            val yNudge = vecToTrident.y * 0.019

            // speed factor based on loyalty
            val d = 0.05 * i

            // smooth/dampen current velocity and add homing component
            val currentVel = trident.velocity
            val newVel = currentVel.multiply(0.95).add(vecToTrident.normalize().multiply(d))

            // apply the tiny Y nudge like NMS did (don't overwrite, just nudge)
            newVel.y += yNudge

            player.velocity = newVel


            if (player.location.distanceSquared(trident.location) <= 2.3 * 2.3) {
                trident.remove()
                it.cancel()
            }
        }
    }


    // val i = 3
    //        trident.setNoPhysics(true)
    //        Executors.syncTimer(0, 1) {
    //
    //            val tridentPos = trident.location.toVector()
    //            val eyePos = player.eyeLocation.toVector()
    //            val vecToPlayer = eyePos.subtract(tridentPos) // direction from trident -> player
    //
    //            val yNudge = vecToPlayer.y * 0.019
    //
    //            // speed factor based on loyalty
    //            val d = 0.05 * i
    //
    //            // smooth/dampen current velocity and add homing component
    //            val currentVel = trident.velocity
    //            val newVel = currentVel.multiply(0.8).add(vecToPlayer.normalize().multiply(d))
    //
    //            // apply the tiny Y nudge like NMS did (don't overwrite, just nudge)
    //            newVel.y += yNudge
    //
    //            trident.velocity = newVel
    //
    //
    //            if (trident.location.distanceSquared(player.location) <= 2.3 * 2.3) {
    //                trident.remove()
    //                it.cancel()
    //            }
    //        }
}
