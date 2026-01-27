package dev.lumas.lumaitems.configuration.serdes

import eu.okaeri.configs.schema.GenericsPair
import eu.okaeri.configs.serdes.BidirectionalTransformer
import eu.okaeri.configs.serdes.SerdesContext
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World

class LocationTransformer : BidirectionalTransformer<String, Location>() {
    override fun getPair(): GenericsPair<String, Location> {
        return this.genericsPair(String::class.java, Location::class.java)
    }

    override fun leftToRight(data: String, serdesContext: SerdesContext): Location {
        val parts: Array<String> = data.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        require(!(parts.size != 4 && parts.size != 6)) { "Invalid location format: $data" }
        val world = Bukkit.getWorld(parts[0])
        val x = parts[1].toInt()
        val y = parts[2].toInt()
        val z = parts[3].toInt()
        if (parts.size == 6) {
            val yaw = parts[4].toFloat()
            val pitch = parts[5].toFloat()
            return Location(world, x.toDouble(), y.toDouble(), z.toDouble(), yaw, pitch)
        }
        return Location(world, x.toDouble(), y.toDouble(), z.toDouble())
    }

    override fun rightToLeft(data: Location, serdesContext: SerdesContext): String {
        val world: World = if (data.world == null) {
            Bukkit.getWorlds()[0]
        } else {
            data.world!!
        }
        if (data.yaw == 0f && data.pitch == 0f) {
            return world.name + "," + data.blockX + "," + data.blockY + "," + data.blockZ
        }
        return world.name + "," + data.blockX + "," + data.blockY + "," + data.blockZ + "," + data.yaw + "," + data.pitch
    }
}
