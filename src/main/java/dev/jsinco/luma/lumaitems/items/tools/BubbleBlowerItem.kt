package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.BukkitVectors
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

//@Ignore
class BubbleBlowerItem : CustomItemFunctions() {


    private val nameSpace = Util.namespacedKey("bubble-blower")
    @Suppress("DuplicatedCode")
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
        snowball.velocity = newDir.multiply(originalVelocity.length()).multiply(0.4)

        snowball.isPersistent = false
        //snowball.setGravity(false)
        snowball.item = ItemStack(Material.NETHER_STAR)
        Util.setPersistentKey(snowball, nameSpace, PersistentDataType.SHORT, 1)

        val radians = listOf(5.0, -5.0, 10.0, -10.0)
        for (angle in radians) {
            cloneSnowball(snowball, BukkitVectors.rotateVectorY(newDir, Math.toRadians(angle)))
                .spawnAt(snowball.location)
        }

//        val boundingBox = snowball.boundingBox
//        val interactionEntity = player.world.spawn(snowball.location, Interaction::class.java)
//        interactionEntity.interactionWidth = boundingBox.widthX.plus(0.2).toFloat()
//        interactionEntity.interactionHeight = -boundingBox.height.plus(0.2).toFloat()
//        interactionEntity.isResponsive = true
//        Util.setPersistentKey(interactionEntity, nameSpace, PersistentDataType.SHORT, 1)
//
//        snowball.addPassenger(interactionEntity)

//        Executors.asyncTimer(0, 1) {task ->
//            if (snowball.isDead) {
//                Executors.sync { interactionEntity.remove() }
//                task.cancel()
//                return@asyncTimer
//            }
//            interactionEntity.teleportAsync(snowball.location)
//        }

    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return

        event.hitEntity?.let { hitEntity ->
            if (hitEntity == snowball.shooter) {
                return
            }
            snowball.world.playSound(snowball.location, Sound.ENTITY_GENERIC_EXPLODE, 0.2f, 1.2f)
            snowball.world.playSound(snowball.location, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 1f, 1f)
            snowball.world.spawnParticle(Particle.EXPLOSION, snowball.location, 1, 0.0, 0.0, 0.0, 0.0)
            snowball.world.spawnParticle(Particle.SOUL_FIRE_FLAME, snowball.location, 10, 0.5, 0.5, 0.5, 0.8)
            snowball.getNearbyEntities(3.0,1.0,3.0).mapNotNull { it as? LivingEntity }.forEach {
                if (!AbilityUtil.noDamagePermission(snowball.shooter as Player, it)) {
                    it.damage(20.0, player)
                    it.noDamageTicks = 0
                }
            }
            snowball.remove()
            return
        }

        if (snowball.location.distance(player.location) > 20 || snowball.velocity.length() <= 0.2) {
            snowball.remove()
            return
        }
        val surface = event.hitBlockFace ?: return
        val newSnowball = cloneSnowball(snowball, BukkitVectors.bounceWithBlockFace(snowball, surface, 0.85))
        newSnowball.spawnAt(snowball.location)
    }

    override fun onEntityDamageGeneric(player: Player, event: EntityDamageEvent) {
        player.sendMessage("hit")
    }
}