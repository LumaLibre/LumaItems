package dev.lumas.lumaitems.model

import com.comphenix.protocol.PacketType
import dev.lumas.lumaitems.hooks.ProtocolLibHook
import dev.lumas.lumaitems.registry.Registry
import java.lang.IllegalStateException
import java.util.UUID
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player

class FakeLightning private constructor(
    val location: Location,
    val viewers: Collection<Player>
) {

    fun strike(): FakeLightning {
        val protocolManager = Registry.HOOKS.getOrThrow(ProtocolLibHook::class).getProtocolManager()
            ?: throw IllegalStateException("ProtocolLib isn't enabled on this server.")
        val packet =  protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY)

        val entityId = Random.nextInt()

        packet.integers.write(0, entityId) // Entity ID
        packet.uuiDs.write(0, UUID.randomUUID()) // Entity UUID
        packet.entityTypeModifier.write(0, EntityType.LIGHTNING_BOLT)
        packet.doubles
            .write(0, location.x)         // X
            .write(1, location.y)         // Y
            .write(2, location.z)         // Z

        for (player in viewers) {
            protocolManager.sendServerPacket(player, packet)
        }
        return this
    }


    companion object {
        @JvmStatic
        fun builder() = Builder()
    }


    class Builder {
        private var location: Location? = null
        private var viewers: MutableCollection<Player> = mutableListOf()
        private var radius: Double? = null

        fun location(location: Location) = apply { this.location = location }
        fun viewers(viewers: Collection<Player>) = apply { this.viewers.addAll(viewers) }
        fun viewersFromRadius(radius: Double) = apply { this.radius = radius }

        fun build(): FakeLightning {
            val loc = location ?: throw IllegalStateException("Location must be set to build a FakeLightning.")

            radius?.let { radi ->
                this.viewers.addAll(
                    loc.world.players.filter {
                        it.location.distanceSquared(loc) <= radi * radi
                    }
                )
            }

            return FakeLightning(loc, viewers)
        }
    }
}