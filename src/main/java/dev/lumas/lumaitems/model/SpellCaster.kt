package dev.lumas.lumaitems.model

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.syncDelayed
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.persistence.PersistentDataType

class SpellCaster private constructor(
    val player: Player,
    val particle: Particle?,
    val key: NamespacedKey,
    val ticks: Long,
    val hideRadius: Double,
    val onTickCallback: ((Snowball) -> Unit)?
) {

    private var snowball: Snowball? = null

    fun isAlive(): Boolean = snowball != null && !snowball!!.isDead
    fun snowballOrThrow(): Snowball = snowball ?: throw IllegalStateException("Snowball is not cast or already dead.")

    fun cast() {
        if (isAlive()) {
            throw IllegalStateException("Already cast and snowball is still alive.")
        }

        val snowball = player.launchProjectile(Snowball::class.java)
            .also { this.snowball = it }

        snowball.setGravity(false)
        snowball.velocity = player.location.direction.multiply(3.0)
        snowball.setPersistentKey(key, PersistentDataType.SHORT, 1)

        for (player in snowball.location.getNearbyPlayers(hideRadius)) {
            player.hideEntity(LumaItems.getInstance(), snowball)
        }


        if (particle != null || onTickCallback != null) {
            Executors.asyncTimer(0, 1) { task ->
                if (snowball.isDead) {
                    task.cancel()
                    return@asyncTimer
                }
                if (particle != null) {
                    snowball.world.spawnParticle(particle, snowball.location, 4, 0.1, 0.1, 0.1, 0.0)
                }
                onTickCallback?.invoke(snowball)
            }
        }
        snowball.syncDelayed(ticks) {
            if (!snowball.isDead) {
                snowball.remove()
            }
            this.snowball = null // remove reference when done
        }
    }


    companion object {
        fun builder() = Builder()
    }


    class Builder {
        private var player: Player? = null
        private var particle: Particle? = null
        private var key: NamespacedKey? = null
        private var ticks: Long = 100
        private var hideRadius: Double = 65.0
        private var onTickCallback: ((Snowball) -> Unit)? = null

        fun player(player: Player) = apply { this.player = player }
        fun particle(particle: Particle?) = apply { this.particle = particle }
        fun key(key: NamespacedKey) = apply { this.key = key }
        fun ticks(ticks: Long) = apply { this.ticks = ticks }
        fun hideRadius(radius: Double) = apply { this.hideRadius = radius }
        fun onTick(callback: (Snowball) -> Unit) = apply { this.onTickCallback = callback }

        fun build(): SpellCaster {
            return SpellCaster(
                player ?: throw IllegalArgumentException("Player must be provided"),
                particle,
                key ?: throw IllegalArgumentException("Key must be provided"),
                ticks,
                hideRadius,
                onTickCallback
            )
        }

        fun runNow(): SpellCaster {
            return build().also { it.cast() }
        }
    }
}