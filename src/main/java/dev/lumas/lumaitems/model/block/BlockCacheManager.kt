package dev.lumas.lumaitems.model.block

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.block.Block

object BlockCacheManager {

    // Instead of using MetaData. I decided to just use a map because this involved
    // fewer checks overall.
    val playerCachedBlocks: ConcurrentHashMap<UUID, PlayerCachedBlocks> = ConcurrentHashMap()


    fun cacheBlock(uuid: UUID, block: Block, id: String) {
        val cachedBlocks = playerCachedBlocks[uuid] ?: PlayerCachedBlocks(id, mutableSetOf())
        cachedBlocks.locations.add(block.location)
        playerCachedBlocks[uuid] = cachedBlocks
    }

    fun cacheBlock(uuid: UUID, blocks: List<Block>, id: String) {
        val cachedBlocks = playerCachedBlocks[uuid] ?: PlayerCachedBlocks(id, mutableSetOf())
        cachedBlocks.locations.addAll(blocks.map { it.location })
        playerCachedBlocks[uuid] = cachedBlocks
    }

    fun unCacheBlock(uuid: UUID, block: Block) {
        val cachedBlocks = playerCachedBlocks[uuid] ?: return
        cachedBlocks.locations.remove(block.location)
        if (cachedBlocks.locations.isEmpty()) {
            playerCachedBlocks.remove(uuid)
        }
    }

    fun unCacheBlock(uuid: UUID, blocks: List<Block>) {
        val cachedBlocks = playerCachedBlocks[uuid] ?: return
        cachedBlocks.locations.removeAll(blocks.map { it.location }.toSet())
        if (cachedBlocks.locations.isEmpty()) {
            playerCachedBlocks.remove(uuid)
        }
    }

    fun getCachedBlocks(uuid: UUID): List<Block> {
        return playerCachedBlocks[uuid]?.getBlocks() ?: emptyList()
    }

    fun getCachedBlocks(id: String): List<Block> {
        return playerCachedBlocks.values.filter { it.id.equals(id, true) }.flatMap { it.getBlocks() }
    }
}