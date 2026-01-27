package dev.lumas.lumaitems.obj

import org.bukkit.Location
import org.bukkit.block.Block

class PlayerCachedBlocks (
    val id: String,
    val locations: MutableList<Location>,
) {
    fun getBlocks(): List<Block> {
        return locations.map { it.block }
    }

    override fun toString(): String {
        return "PlayerCachedBlocks(id='$id', locations=${locations.size})"
    }

}