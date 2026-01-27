package dev.lumas.lumaitems.util

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.util.extensions.BlockUtil.breakNaturallyWithLog
import io.lumine.mythic.bukkit.MythicBukkit
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.UUID
import kotlin.random.Random


object AbilityUtil {

    val plugin: LumaItems = LumaItems.getInstance()
    private val blockedAbility: MutableSet<UUID> = mutableSetOf()
    private val AIR = ItemStack(Material.AIR)

    @Suppress("deprecation", "removal")
    @JvmStatic
    fun noDamagePermission(attacker: Player, victim: Entity): Boolean {
        val event = EntityDamageByEntityEvent(attacker, victim, EntityDamageEvent.DamageCause.ENTITY_ATTACK, 0.1)
        Bukkit.getPluginManager().callEvent(event)
        return event.isCancelled
    }

    @JvmStatic // TODO: Replace this with proper towny and worldguard integration
    fun noBuildPermission(player: Player, block: Block): Boolean {
        val event = BlockPlaceEvent(block, block.state, block.getRelative(BlockFace.DOWN), AIR, player, true, EquipmentSlot.HAND)
        return !event.callEvent()
    }

    @JvmStatic
    fun noBreakPermission(player: Player, block: Block): Boolean {
        val event = BlockBreakEvent(block, player).apply { isDropItems = false }
        Bukkit.getPluginManager().callEvent(event)
        return event.isCancelled
    }

    fun getDirectionBetweenLocations(start: Location, end: Location): Vector {
        val from = start.toVector()
        val to = end.toVector()
        return to.subtract(from)
    }

    fun findMostCommonItem(collection: Collection<ItemStack>): ItemStack {
        return collection.groupingBy { it }
            .eachCount()
            .maxBy { it.value }
            .key
    }

    fun isOnGround(entity: Entity): Boolean {
        return entity.location.add(0.0,-0.1, 0.0).block.isSolid
    }

    fun Player.isLocationOnGround(): Boolean {
        return this.location.add(0.0,-0.1, 0.0).block.isSolid
    }

    fun Player.isLocationOnGround(amt: Double): Boolean {
        return this.location.add(0.0,-amt, 0.0).block.isSolid
    }

    fun Player.isLocationOnGround(amt: Double, isAir: Boolean): Boolean {
        val block = this.location.add(0.0,-amt, 0.0).block
        return if (isAir) block.isEmpty else block.isSolid
    }

    fun isJobsTracked(block: Block): Boolean {
        return block.state.hasMetadata("BlockOwner") || block.state.hasMetadata("JobsExploit")
    }

    fun breakRelativeBlock(block: Block, player: Player, particle: Particle?, type: String, limiterInitial: Int) {
        var limiter = limiterInitial
        if (blockedAbility.contains(player.uniqueId)) return
        val faces =
            arrayOf(BlockFace.DOWN, BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST)
        //Loop through all block faces (All 6 sides around the block)
        if (limiter > 8) return
        // edit this
        for (face in faces) {
            val b = block.getRelative(face)
            if (b.type.toString().lowercase().contains(type)) {
                if (particle != null) {
                    b.world.spawnParticle(Particle.BLOCK, b.location, 5, 0.5, 0.5, 0.5, 0.1, b.blockData)
                    b.world.spawnParticle(particle, b.location, 2, 0.5, 0.5, 0.5, 0.1)
                }
                blockedAbility.add(player.uniqueId)
                player.breakBlock(b)
                blockedAbility.remove(player.uniqueId)
                block.breakNaturallyWithLog(player, player.inventory.itemInMainHand)
                if (type == "leaves") {
                    limiter++
                }
                val finalLimiter = limiter
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin,
                    { breakRelativeBlock(b, player, particle, type, finalLimiter) }, 1L
                )
            }
        }
    }


    fun spawnSpell(player: Player, particle: Particle?, key: NamespacedKey, ticksAlive: Long): Snowball {
        return spawnSpell(player, particle, key, ticksAlive, null)
    }

    fun spawnSpell(player: Player, particle: Particle?, key: String, ticksAlive: Long): Snowball {
        return spawnSpell(player, particle, Util.namespacedKey(key), ticksAlive, null)
    }

    fun spawnSpell(player: Player, particle: Particle?, key: String, ticksAlive: Long, runnableCallback: EntityCallBack?): Snowball {
        return spawnSpell(player, particle, Util.namespacedKey(key), ticksAlive, runnableCallback)
    }

    fun spawnSpell(player: Player, particle: Particle?, key: NamespacedKey, ticksAlive: Long, runnableCallback: EntityCallBack?): Snowball {
        val snowball = player.launchProjectile(Snowball::class.java)
        snowball.setGravity(false)
        snowball.velocity = player.location.direction.multiply(3)
        snowball.persistentDataContainer.set(key, PersistentDataType.SHORT, 1)
        player.hideEntity(plugin, snowball)
        for (entity in player.getNearbyEntities(65.0, 65.0, 65.0)) {
            if (entity is Player) {
                entity.hideEntity(plugin, snowball)
            }
        }

        if (particle != null || runnableCallback != null) {
            object : BukkitRunnable() {
                override fun run() {
                    if (snowball.isDead) {
                        cancel()
                    }
                    if (particle != null) {
                        snowball.world.spawnParticle(particle, snowball.location, 4, 0.1, 0.1, 0.1, 0.0)
                    }
                    runnableCallback?.go(snowball)
                }
            }.runTaskTimerAsynchronously(plugin, 0, 1)
        }
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            if (!snowball.isDead) {
                snowball.remove()
            }
        }, ticksAlive)
        return snowball
    }


    fun takeSpellLapisCost(player: Player, amount: Int): Boolean {
        if (player.inventory.contains(Material.LAPIS_LAZULI, amount)) {
            player.inventory.removeItem(ItemStack(Material.LAPIS_LAZULI, amount))
            return true
        }
        return false
    }

    fun damageOverTicks(victim: LivingEntity, attacker: Player?, damage: Double, hitAmount: Int): Int {
        return damageOverTicks(victim, attacker, damage, hitAmount, null)
    }

    fun damageOverTicks(victim: LivingEntity, attacker: Player?, damage: Double, hitAmount: Int, runnableCallback: EntityCallBack?): Int {
        return damageOverTicks(victim, attacker, damage, hitAmount, runnableCallback, null)
    }

    fun damageOverTicks(victim: LivingEntity, attacker: Player?, damage: Double, hitAmount: Int, runnableCallback: EntityCallBack?, whenFinishedCallback: EntityCallBack?): Int {
        val damageToDealOverTicks = damage / hitAmount
        object : BukkitRunnable() {
            var count = 0
            override fun run() {
                if (count >= hitAmount || victim.isDead) {
                    this.cancel()
                    whenFinishedCallback?.go(victim)
                    return
                }
                victim.damage(damageToDealOverTicks, attacker)
                runnableCallback?.go(victim)
                count++
            }
        }.runTaskTimer(plugin, 0, 10)
        return hitAmount * 10
    }
}