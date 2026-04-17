package dev.lumas.lumaitems.model.block

import org.bukkit.Location
import org.bukkit.block.Block

class PlayerCachedBlocks (
    val id: String,
    val locations: MutableSet<Location>,
) {
    fun getBlocks(): List<Block> {
        return locations.map { it.block }
    }

    override fun toString(): String {
        return "PlayerCachedBlocks(id='$id', locations=${locations.size})"
    }

}