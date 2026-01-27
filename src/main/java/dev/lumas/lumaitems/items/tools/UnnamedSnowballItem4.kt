package dev.lumas.lumaitems.items.tools

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class UnnamedSnowballItem4 : CustomItemFunctions() {

    // Purpose of this item is to spawn "bubbles" (snowballs) which infinitely float around the player.
    // The player can "hit" these bubbles to pop them and cause them to explode

    val nameSpace = Util.namespacedKey("unnamed-snowball-4")

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Unnamed Snowball 4")
            .material(org.bukkit.Material.SNOWBALL)
            .persistentData(nameSpace)
            .buildPair()
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val snowball = event.entity as? Snowball ?: return
        snowball.velocity = snowball.velocity.multiply(0.5)
        snowball.setGravity(false)
        Util.setPersistentKey(snowball, nameSpace, PersistentDataType.SHORT, 1)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {

        val snowball = event.entity as? Snowball ?: return

        val newSnowball = newSnowball(snowball, event.hitBlockFace ?: return)
        newSnowball.spawnAt(snowball.location)
    }

    private fun getNewPhysics(snowball: Snowball, surface: BlockFace): Vector {
        val velocity = snowball.velocity
        val normal = Vector(surface.modX.toDouble(), surface.modY.toDouble(), surface.modZ.toDouble()).normalize()

        val dot = velocity.dot(normal)
        val reflected = velocity.clone().subtract(normal.clone().multiply(2 * dot))


        return reflected.normalize().multiply(velocity.length() * 2.0)
    }


    private fun newSnowball(snowball: Snowball, hitSurface: BlockFace): Snowball {
        val newSnowball = snowball.world.createEntity(snowball.location, Snowball::class.java)
        newSnowball.velocity = getNewPhysics(snowball, hitSurface)
        newSnowball.shooter = snowball.shooter
        newSnowball.item = snowball.item
        newSnowball.setGravity(snowball.hasGravity())
        newSnowball.isPersistent = false
        Util.setPersistentKey(newSnowball, nameSpace, PersistentDataType.SHORT, 1)


        return newSnowball
    }
}