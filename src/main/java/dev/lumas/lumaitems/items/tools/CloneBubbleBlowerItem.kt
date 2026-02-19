package dev.lumas.lumaitems.items.tools

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.setAirWithLog
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.entity.Interaction
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

// TODO

class CloneBubbleBlowerItem : CustomItemFunctions() {


    companion object {
        private val nameSpace = Util.namespacedKey("bubble-blower")
        private val BUNDLES = Tag.ITEMS_BUNDLES.values

        private val RECURSIVE_EXPLODE_LIMIT = 4
    }

    private val cloneSnowball = fun(snowball: Snowball, newVelocity: Vector): Snowball {
        val newSnowball = snowball.world.createEntity(snowball.location, Snowball::class.java)
        newSnowball.velocity = newVelocity
        newSnowball.shooter = snowball.shooter
        newSnowball.item = snowball.item
        newSnowball.setGravity(snowball.hasGravity())
        newSnowball.isPersistent = false
        snowball.passengers.firstOrNull()?.let {
            newSnowball.addPassenger(it)
        }
        Util.setPersistentKey(newSnowball, nameSpace, PersistentDataType.SHORT, 1)
        return newSnowball
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Bubble Blower")
            .material(Material.DIAMOND_AXE)
            .persistentData(nameSpace)
            .buildPair()
    }


    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        if (player.attackCooldown < 0.95f) return

        val snowball = player.launchProjectile(Snowball::class.java)
        val originalVelocity = snowball.velocity

        val originalDir = originalVelocity.clone().normalize()
        val newDir = BukkitVectors.rotateVectorY(originalDir, Math.toRadians(random().nextDouble(-5.0, 5.0)))
        snowball.velocity = newDir.multiply(originalVelocity.length()).multiply(0.09)

        snowball.isPersistent = false
        snowball.setGravity(false)
        snowball.item = ItemStack(BUNDLES.random())
        Util.setPersistentKey(snowball, nameSpace, PersistentDataType.SHORT, 1)


        val boundingBox = snowball.boundingBox
        val interactionEntity = player.world.spawn(snowball.location, Interaction::class.java)
        interactionEntity.interactionWidth = boundingBox.widthX.plus(0.2).toFloat()
        interactionEntity.interactionHeight = -boundingBox.height.plus(0.2).toFloat()
        interactionEntity.isResponsive = true
        Util.setPersistentKey(interactionEntity, nameSpace, PersistentDataType.SHORT, 1)

        snowball.addPassenger(interactionEntity)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return

        if (snowball.location.distance(player.location) > 20) {
            snowball.remove()
            return
        }
        val surface = event.hitBlockFace ?: return
        val newSnowball = cloneSnowball(snowball, BukkitVectors.bounceWithBlockFace(snowball, surface, 1.0))
        newSnowball.spawnAt(snowball.location)
    }

    override fun onEntityDamageGeneric(player: Player, event: EntityDamageEvent) {
        val interaction = event.entity as? Interaction ?: return
        val snowball = interaction.vehicle as? Snowball ?: return

        chainExplode(player, snowball)
    }

    fun chainExplode(player: Player, snowball: Snowball, radius: Double = 5.0, density: Double = 20.0, limit: Int = 0) {
        val location = snowball.location
        val sphere = Sphere(location, radius, density)
        sphere.sphere.forEach { block ->
            block.setAirWithLog(player)
        }

        snowball.passengers.forEach { it.remove() }
        snowball.remove()


        if (limit >= RECURSIVE_EXPLODE_LIMIT) return


        val nearbyExplodables = location.getNearbyEntitiesByType(Snowball::class.java, radius).filter {
            Util.hasPersistentKey(it, nameSpace)
        }.toMutableList()

        nearbyExplodables.forEach { otherSnowball ->
            val newLoc = otherSnowball.location
            chainExplode(player, otherSnowball, radius * 0.75, density, limit + 1)
        }

    }

}