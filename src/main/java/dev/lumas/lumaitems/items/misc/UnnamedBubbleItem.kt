package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.disabling.Ignore
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncTimer
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.UUID
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Ignore
class UnnamedBubbleItem : CustomItemFunctions() {

    companion object {

        const val MAX_BLOBS = 8

        private val nameSpace = Util.namespacedKey("lite-blobber-hammer")
        private val processes: MutableMap<UUID, BlobGroup> = mutableMapOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("in progress")
            .material(Material.RED_DYE)
            .persistentData(nameSpace)
            .buildPair()
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return
        val hitSurface = event.hitBlockFace ?: return
        if (!processes.containsKey(player.uniqueId)) {
            return
        }

        val group = processes[player.uniqueId] ?: return
        val blob = group.getOwnedBlob(snowball) ?: return

        blob.bounce(hitSurface)
    }

    override fun asyncGlobalTask() {
        val players = instance().server.onlinePlayers.filter {
            it.sync {
                Util.isItemInSlot(nameSpace, EquipmentSlot.HAND, it)
            }
        }
        // destroy and remove groups for players that are not in ths list
        processes.keys.filter { it !in players.map { p -> p.uniqueId } }.forEach { uuid ->
            processes[uuid]?.destroy()
            println("Destroying blob group for player: ${uuid}")
            processes.remove(uuid)
        }

        players.forEach { player ->
            player.sync {
                val group = processes[player.uniqueId] ?: run {
                    val newGroup = BlobGroup(player)
                    processes[player.uniqueId] = newGroup
                    newGroup.initiateGroup()
                    return@sync
                }
                group.updatePositions()
            }
        }
    }


    class BlobGroup(
        val player: Player,

    ) {
        private val blobs: MutableSet<Blob> = mutableSetOf()

        fun initiateGroup() {
            println("Initiating blob group for player: ${player.name}")
            val spawnLocations = hashedSpawnLocations(player.eyeLocation, player.uniqueId)
            for (i in 0 until MAX_BLOBS) {
                val blob = Blob(player, spawnLocations[i])
                blobs.add(blob)
                blob.move()
            }
        }

        fun updatePositions() {
            val spawnLocations = hashedSpawnLocations(player.eyeLocation, player.uniqueId)
            for ((index, blob) in blobs.withIndex()) {
                if (!blob.valid) {
                    continue
                }
                if (index <= spawnLocations.size) {
                    blob.position = spawnLocations[index]
                    blob.move()
                }
            }
        }

        fun getOwnedBlob(snowball: Snowball): Blob? {
            for (blob in blobs) {
                if (blob.snowball.uniqueId == snowball.uniqueId) {
                    return blob
                }
            }
            return null
        }

        fun destroy() {
            for (blob in blobs) {
                blob.valid = false
                blob.snowball.remove()
                blob.moveTask?.cancel()
            }
            blobs.clear()
        }


        private fun hashedSpawnLocations(originalLocation: Location, uuid: UUID): List<Location> {
            val seed = uuid.mostSignificantBits xor uuid.leastSignificantBits
            val random = Random(seed) // always produces same sequence for same UUID

            val locations = mutableListOf<Location>()
            repeat(MAX_BLOBS) {
                val modX = random.nextDouble(-3.0, 3.0)
                val modY = random.nextDouble(-1.0, 3.0)
                val modZ = random.nextDouble(-3.0, 3.0)

                locations.add(originalLocation.clone().add(modX, modY, modZ))
            }
            return locations
        }
    }


    class Blob(
        val player: Player,
        @Volatile var position: Location,
    ) {
        @Volatile var snowball = createSnowball()
        var moveTask: ScheduledTask? = null
        var valid = true

        fun createSnowball(): Snowball {
            val created = position.world.spawn(position, Snowball::class.java)
            created.isPersistent = false
            created.setGravity(false)
            created.shooter = player
            Util.setPersistentKey(created, nameSpace, PersistentDataType.SHORT, 1)
            return created
        }

        fun bounce(hitSurface: BlockFace) {
            val vector = BukkitVectors.bounceWithBlockFace(snowball, hitSurface, 1.0)
            snowball = createSnowball().apply {
                velocity = vector
            }
        }

        fun move() {
            if ((moveTask != null && !moveTask!!.isCancelled) || !valid) {
                return
            }
            this.moveTask = snowball.syncTimer(0, 1) { task ->
                if (!valid) {
                    task.cancel()
                    return@syncTimer
                }
                if (snowball.location.world != position.world || snowball.location.distance(position) <= 0.5) {
                    snowball.velocity = BukkitVectors.ZERO
                    return@syncTimer
                }
                val direction = position.clone().subtract(snowball.location).toVector().normalize()
                snowball.velocity = direction.normalize()
            } ?: return
        }
    }
}