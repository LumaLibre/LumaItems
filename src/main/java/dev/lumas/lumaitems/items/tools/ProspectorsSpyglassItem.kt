package dev.lumas.lumaitems.items.tools

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.task.Synchronizable
import dev.lumas.lumaitems.util.PacketGlowColors
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.getOreColor
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.tags.Kind
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Vector3f

class ProspectorsSpyglassItem : CustomItemFunctions() {

    companion object {
        private val DISPLAYABLE_BLOCKS = ConcurrentLinkedQueue<DisplayableBlock>()
        private val TRANSLATION = Vector3f(0.001f, 0.0002f, 0.001f)
        private val KEY = Util.namespacedKey("prospectors-spyglass")
        private const val SCALAR = 0.995f
        private const val RANGE = 18.0
        private const val CULL_RANGE = RANGE + 2.0
        private const val RENDER_RANGE = 0.3f // The display view range is a multiplier of 64 blocks
        private const val CONE_COSINE = 0.9981348 // cos(3.5 degrees)
        private val CONE_TANGENT = sqrt(1.0 - (CONE_COSINE * CONE_COSINE)) / CONE_COSINE
        private const val CONE_NEAR_RADIUS = 0.87 // Half of a block's diagonal

        // How far behind the eye the apex sits to give the cone CONE_NEAR_RADIUS of width at the eye
        private val CONE_APEX_OFFSET = CONE_NEAR_RADIUS / CONE_TANGENT
        private const val SCAN_INTERVAL_TICKS = 4

        private fun inCone(dx: Double, dy: Double, dz: Double, direction: Vector): Boolean {
            val forward = (dx * direction.x) + (dy * direction.y) + (dz * direction.z)
            if (forward < 0.0) return false // the apex sits behind the eye, but nothing behind the player is revealed

            val lateralSquared = (dx * dx) + (dy * dy) + (dz * dz) - (forward * forward)
            val radius = (forward + CONE_APEX_OFFSET) * CONE_TANGENT

            return lateralSquared <= radius * radius
        }

        private fun packed(x: Int, y: Int, z: Int): Long {
            return ((x.toLong() and 0x3FFFFFF) shl 38) or ((z.toLong() and 0x3FFFFFF) shl 12) or (y.toLong() and 0xFFF)
        }
    }

    private val scanTicks = ConcurrentHashMap<UUID, Int>()

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#1e8abf:#9be4df:#EDB172:#ffe494>Prospector's Spyglass</gradient></b>")
            .vanillaEnchants(Enchantment.PIERCING to 10)
            .customEnchants("<#9be4df>Assay")
            .material(Material.SPYGLASS)
            .persistentData(KEY)
            .tier(Tier.LUMARINE_2026)
            .lore(
                "<#9be4df>Scope in</#9be4df> to reveal ores",
                "through solid ground.",
                "",
                "Reaches far further than",
                "a mattock, but only ever",
                "where you point it."
            )
            .buildPair()
    }

    override fun asyncGlobalTask() {
        for (displayableBlock in DISPLAYABLE_BLOCKS) {
            val player = displayableBlock.ownerAsPlayer()
            if (player == null || !this.isScoping(player)) {
                displayableBlock.remove()
            }
        }
    }

    override fun onFastAsyncRunnable(player: Player) {
        if (!this.isScoping(player)) {
            this.scanTicks.remove(player.uniqueId)
            this.clearDisplayableBlocks(player)
            return
        }

        val eye = player.eyeLocation
        val direction = eye.direction

        for (displayableBlock in DISPLAYABLE_BLOCKS) {
            if (displayableBlock.owner != player.uniqueId) continue
            if (!displayableBlock.isVisibleFrom(eye, direction)) {
                displayableBlock.remove()
            }
        }

        val ticks = this.scanTicks.merge(player.uniqueId, 1, Int::plus) ?: 1
        if (ticks % SCAN_INTERVAL_TICKS != 0) return

        player.sync {
            getOresInSight(player, eye, direction)
                .takeIf { it.isNotEmpty() }
                ?.apply { DISPLAYABLE_BLOCKS.addAll(this) }
        }
    }

    override fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {
        val item = player.inventory.getItem(event.newSlot)

        if (item == null || !Util.hasPersistentKey(item, KEY)) {
            this.clearDisplayableBlocks(player)
        }
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        this.clearDisplayableBlocks(player)
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        this.scanTicks.remove(player.uniqueId)
        this.clearDisplayableBlocks(player)
    }

    override fun onPluginDisableGlobal() {
        DISPLAYABLE_BLOCKS.forEach { it.remove() }
        DISPLAYABLE_BLOCKS.clear()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        DISPLAYABLE_BLOCKS.forEach {
            if (it.isAtBlock(event.block)) {
                it.remove()
            }
        }
    }

    private fun isScoping(player: Player): Boolean {
        return player.isValid && player.hasActiveItem() && player.activeItem.isMatchingItem(KEY)
    }

    private fun getOresInSight(player: Player, eye: Location, direction: Vector): List<DisplayableBlock> {
        val world = eye.world
        val list = mutableListOf<DisplayableBlock>()
        val occupied = HashSet<Long>()

        for (displayableBlock in DISPLAYABLE_BLOCKS) {
            if (displayableBlock.world == world) {
                occupied.add(packed(displayableBlock.x, displayableBlock.y, displayableBlock.z))
            }
        }

        val minX = floor(eye.x - RANGE).toInt()
        val maxX = floor(eye.x + RANGE).toInt()
        val minY = max(floor(eye.y - RANGE).toInt(), world.minHeight)
        val maxY = min(floor(eye.y + RANGE).toInt(), world.maxHeight - 1)
        val minZ = floor(eye.z - RANGE).toInt()
        val maxZ = floor(eye.z + RANGE).toInt()

        var lastChunkX = Int.MIN_VALUE
        var lastChunkZ = Int.MIN_VALUE
        var chunkLoaded = false

        for (x in minX..maxX) {
            val dx = x + 0.5 - eye.x
            for (z in minZ..maxZ) {
                val dz = z + 0.5 - eye.z

                val chunkX = x shr 4
                val chunkZ = z shr 4
                if (chunkX != lastChunkX || chunkZ != lastChunkZ) {
                    lastChunkX = chunkX
                    lastChunkZ = chunkZ
                    chunkLoaded = world.isChunkLoaded(chunkX, chunkZ)
                }
                if (!chunkLoaded) continue

                for (y in minY..maxY) {
                    val dy = y + 0.5 - eye.y
                    if ((dx * dx) + (dy * dy) + (dz * dz) > RANGE * RANGE) continue
                    if (!inCone(dx, dy, dz, direction)) continue
                    if (occupied.contains(packed(x, y, z))) continue

                    val block = world.getBlockAt(x, y, z)
                    if (!Kind.INCLUSIVE_ORES.isTagged(block.type)) continue

                    occupied.add(packed(x, y, z))
                    list.add(displayableBlock(block, player))
                }
            }
        }
        return list
    }

    private fun displayableBlock(block: Block, player: Player): DisplayableBlock {
        val blockData = block.blockData
        val blockDisplay = block.world.spawn(block.location, BlockDisplay::class.java).apply {
            this.block = blockData
            glowColorOverride = block.getOreColor() ?: blockData.mapColor
            interpolationDelay = -1
            interpolationDuration = 0
            isPersistent = false
            viewRange = RENDER_RANGE

            transformation = transformation.apply {
                scale.mul(SCALAR)
                translation.add(TRANSLATION)
            }
        }

        val displayableBlock = DisplayableBlock(player.uniqueId, blockDisplay, CULL_RANGE, block)

        PacketGlowColors.setProtocolGlowPacket(player, blockDisplay, true)
        return displayableBlock
    }

    private fun clearDisplayableBlocks(player: Player) {
        DISPLAYABLE_BLOCKS.filter { it.owner == player.uniqueId }
            .forEach { it.remove() }
    }


    private class DisplayableBlock(
        val owner: UUID,
        val blockDisplay: BlockDisplay,
        val range: Double,
        override val x: Int,
        val y: Int,
        override val z: Int,
        override val world: World
    ) : Synchronizable.BlockPos {

        constructor(owner: UUID, blockDisplay: BlockDisplay, range: Double, block: Block) :
                this(owner, blockDisplay, range, block.x, block.y, block.z, block.world)

        fun ownerAsPlayer(): Player? = Bukkit.getPlayer(owner)

        fun remove() {
            blockDisplay.sync { blockDisplay.remove() }
            DISPLAYABLE_BLOCKS.remove(this)
        }

        fun isVisibleFrom(eye: Location, direction: Vector): Boolean {
            if (eye.world != world) return false

            val dx = x + 0.5 - eye.x
            val dy = y + 0.5 - eye.y
            val dz = z + 0.5 - eye.z

            if ((dx * dx) + (dy * dy) + (dz * dz) > range * range) return false
            return inCone(dx, dy, dz, direction)
        }

        fun isAtBlock(block: Block): Boolean {
            return block.x == x && block.y == y && block.z == z && block.world == world
        }
    }
}
