package dev.lumas.lumaitems.items.misc.builderswand

import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.canBuild
import dev.lumas.lumaitems.util.extensions.flagFor
import dev.lumas.lumaitems.util.extensions.isFlagged
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.Tier
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent
import java.util.PriorityQueue
import kotlin.math.abs
import org.bukkit.ChunkSnapshot
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.data.MultipleFacing
import org.bukkit.block.data.Orientable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class BuildersWandItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#7D9FFC:#CDA9FF:#E28BDC:#F56868>Builder's Wand</gradient></b>")
            .customEnchants("<#CDA9FF>Architex")
            .material(Material.BREEZE_ROD)
            .persistentData(BUILDERS_WAND_KEY)
            .tier(Tier.WONDERLAND_2026)
            .tagline("#CDA9FF", "It's essential!")
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.KNOCKBACK to 2)
            .lore(
                "<#CDA9FF>Right-click</#CDA9FF> to enter",
                "build mode. <#CDA9FF>Left-click</#CDA9FF>",
                "to confirm a selection",
                "or press your <#CDA9FF>swap</#CDA9FF>",
                "<#CDA9FF>key (F)</#CDA9FF> to cancel.",
                "",
                "<#CDA9FF>Left-click</#CDA9FF> a direction",
                "based-block to rotate",
                "it to its next face.",
                "",
                "<red>Cooldown: 4s"
            )
            .buildPair()
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        //if (player.isBuildersWandAddon()) return // hand off to addon

        event.isCancelled = true
        if (player.isFlagged(this)) return
        player.flagFor(this, 1)

        val visualizer = WandVisualizers.take(player)
        if (visualizer != null) {
            commitWandBuild(visualizer, player, 20L * 4)
        } else {
            val clickedBlock = event.clickedBlock ?: return
            if (!player.canBuild(clickedBlock.location)) return
            val blockData = clickedBlock.blockData
            doHandleFaces(blockData, clickedBlock, event.blockFace)
        }
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isBuildersWandAddon()) return

        val clickedBlock = event.clickedBlock ?: return
        val clickedFace = event.blockFace
        event.isCancelled = true

        if (!player.canBuild(clickedBlock.location) || player.isOnCooldown(this) || WAND_BLACKLIST.invoke(clickedBlock)) {
            val relative = clickedBlock.getRelative(clickedFace).location.toCenterLocation()
            relative.spawnWandDust()
            return
        }

        val visualizer = PathVisualizer(player, clickedBlock, this)
        visualizer.start(clickedFace)
        WandVisualizers.put(player, visualizer)
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        WandVisualizers.take(player)?.let { event.isCancelled = true }
    }

    // Drives cleanup for the whole wand family
    override fun asyncGlobalTask() = WandVisualizers.cleanupInvalid()

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        WandVisualizers.take(player)
    }

    override fun onPlayerTeleport(player: Player, event: PlayerTeleportEvent) {
        WandVisualizers.take(player)
    }

    override fun onCanvasAsyncPlayerTeleport(player: Player, event: EntityTeleportAsyncEvent) {
        WandVisualizers.take(player)
    }

    override fun onPluginDisable(player: Player) {
        WandVisualizers.take(player)
    }

    private fun doHandleFaces(blockData: BlockData, clickedBlock: Block, clickedFace: BlockFace) {
        when (blockData) {
            is Directional -> {
                val faces = blockData.faces.toList()
                val nextIndex = (faces.indexOf(blockData.facing) + 1) % faces.size
                blockData.facing = faces[nextIndex]
            }
            is Orientable -> {
                val axes = blockData.axes.toList()
                val nextIndex = (axes.indexOf(blockData.axis) + 1) % axes.size
                blockData.axis = axes[nextIndex]
            }
            is MultipleFacing -> {
                if (clickedFace in blockData.allowedFaces) {
                    blockData.setFace(clickedFace, !blockData.hasFace(clickedFace))
                }
            }
            // TODO: RedstoneWire
        }
        clickedBlock.blockData = blockData
    }


    private class PathVisualizer(
        player: Player,
        origin: Block,
        item: CustomItem,
        private val maxLength: Int = 48,
        private val reachLength: Int = 48
    ) : WandVisualizer(player, origin, item) {

        override fun isStillEquipped(player: Player) = player.isItemInSlot(BUILDERS_WAND_KEY, EquipmentSlot.HAND)

        override fun start(face: BlockFace) {
            val startPos = origin.getRelative(face).location.toCenterLocation()
            var lastTarget: Block? = null
            var lastFace: BlockFace? = null

            task = player.syncTimer(0, 3) {

                val rayResult = player.rayTraceBlocks(reachLength.toDouble()) ?: return@syncTimer
                val target = rayResult.hitBlock ?: return@syncTimer
                val targetFace = rayResult.hitBlockFace ?: return@syncTimer

                if (target == lastTarget && targetFace == lastFace) return@syncTimer
                lastTarget = target
                lastFace = targetFace

                val targetPos = target.getRelative(targetFace).location.toCenterLocation()
                val startNode = startPos.block.location.toCenterLocation()
                val endNode = targetPos.block.location.toCenterLocation()
                val snapshots = buildChunkSnapshots(startPos, targetPos)

                Executors.async {
                    val path = findPath(startNode, endNode, snapshots)

                    // use new path if valid
                    val renderPath = if (path.isNotEmpty()) {
                        lastPath = path
                        path
                    } else {
                        lastPath
                    }

                    player.sync { render(renderPath) }
                }
            }
        }

        fun findPath(start: Location, end: Location, snapshots: Map<Pair<Int, Int>, ChunkSnapshot>): List<Location> {
            // quick reject if too far apart
            if (heuristic(start, end) > maxLength) return emptyList()

            val openSet = PriorityQueue<Node>(compareBy { it.f })
            val cameFrom = mutableMapOf<Location, Location>()
            val gScore = mutableMapOf<Location, Double>()
            var iterations = 0

            gScore[start] = 0.0
            openSet.add(Node(start, heuristic(start, end)))

            while (openSet.isNotEmpty()) {
                if (iterations++ > maxLength * 10) return emptyList()

                val current = openSet.poll()

                if (current.loc.blockX == end.blockX &&
                    current.loc.blockY == end.blockY &&
                    current.loc.blockZ == end.blockZ) {
                    val path = reconstructPath(cameFrom, current.loc)
                    return if (path.size > maxLength) emptyList() else path
                }

                for (neighbor in getNeighbors(current.loc, snapshots)) {
                    val tentativeG = (gScore[current.loc] ?: Double.MAX_VALUE) + 1.0

                    // Skip if already too far
                    if (tentativeG > maxLength) continue

                    if (tentativeG < (gScore[neighbor] ?: Double.MAX_VALUE)) {
                        cameFrom[neighbor] = current.loc
                        gScore[neighbor] = tentativeG
                        openSet.add(Node(neighbor, tentativeG + heuristic(neighbor, end)))
                    }
                }
            }

            return emptyList()
        }

        fun heuristic(a: Location, b: Location): Double {
            // manhattan distance for grid
            return (abs(a.blockX - b.blockX) + abs(a.blockY - b.blockY) + abs(a.blockZ - b.blockZ)).toDouble()
        }

        fun getNeighbors(loc: Location, snapshots: Map<Pair<Int, Int>, ChunkSnapshot>): List<Location> {
            return listOf(
                loc.clone().add(BukkitVectors.EAST),
                loc.clone().add(BukkitVectors.WEST),
                loc.clone().add(BukkitVectors.UP),
                loc.clone().add(BukkitVectors.DOWN),
                loc.clone().add(BukkitVectors.SOUTH),
                loc.clone().add(BukkitVectors.NORTH)
            ).filter { !isSolid(it.blockX, it.blockY, it.blockZ, snapshots) }
        }

        private fun reconstructPath(cameFrom: Map<Location, Location>, end: Location): List<Location> {
            val path = mutableListOf(end)
            var current = end

            while (cameFrom.containsKey(current)) {
                current = cameFrom[current]!!
                path.add(0, current)
            }

            return path
        }


        private fun buildChunkSnapshots(start: Location, end: Location): Map<Pair<Int, Int>, ChunkSnapshot> {
            val snapshots = mutableMapOf<Pair<Int, Int>, ChunkSnapshot>()
            val padding = 2
            val minX = (minOf(start.blockX, end.blockX) - padding) shr 4
            val maxX = (maxOf(start.blockX, end.blockX) + padding) shr 4
            val minZ = (minOf(start.blockZ, end.blockZ) - padding) shr 4
            val maxZ = (maxOf(start.blockZ, end.blockZ) + padding) shr 4

            for (cx in minX..maxX) {
                for (cz in minZ..maxZ) {
                    val chunk = start.world.getChunkAt(cx, cz)
                    snapshots[Pair(cx, cz)] = chunk.chunkSnapshot
                }
            }
            return snapshots
        }

        private fun isSolid(x: Int, y: Int, z: Int, snapshots: Map<Pair<Int, Int>, ChunkSnapshot>): Boolean {
            val cx = x shr 4
            val cz = z shr 4
            val snapshot = snapshots[Pair(cx, cz)] ?: return true // assume solid if not in snapshot
            val type = snapshot.getBlockType(x and 15, y, z and 15)
            return type.isSolid
        }

        data class Node(val loc: Location, val f: Double)
    }
}
