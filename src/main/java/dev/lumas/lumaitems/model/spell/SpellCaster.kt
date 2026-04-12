package dev.lumas.lumaitems.model.spell

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.model.item.PersistentDataRecord
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.syncDelayed
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

class SpellCaster private constructor(
    val player: Player,
    val particle: Particle?,
    val particleData: Any?,
    val key: NamespacedKey,
    val ticks: Long,
    val hideRadius: Double,
    val onTickCallback: ((Snowball) -> Unit)?,
    val spells: List<PersistentDataRecord<*, *>> = emptyList()
) {

    private var snowball: Snowball? = null

    fun isAlive(): Boolean = snowball != null && !snowball!!.isDead
    fun snowballOrThrow(): Snowball = snowball ?: throw IllegalStateException("Snowball is not cast or already dead.")

    fun cast() {
        if (isAlive()) {
            throw IllegalStateException("Already cast and snowball is still alive.")
        }

        val snowball = player.launchProjectile(Snowball::class.java)
            //.apply { velocity = velocity.multiply(0.7) }
            .also { this.snowball = it }

        snowball.setGravity(false)
        snowball.velocity = player.location.direction.multiply(3.0)
        snowball.setPersistentKey(key, PersistentDataType.SHORT, 1)

        for (player in snowball.location.getNearbyPlayers(hideRadius)) {
            player.hideEntity(LumaItems.getInstance(), snowball)
        }

        for (record in spells) {
            setPersistentData(snowball.persistentDataContainer, record)
        }


        if (particle != null || onTickCallback != null) {
            Executors.asyncTimer(0, 1) { task ->
                if (snowball.isDead) {
                    task.cancel()
                    return@asyncTimer
                }
                if (particle != null) {
                    snowball.world.spawnParticle(particle, snowball.location, 4, 0.1, 0.1, 0.1, 0.0, particleData)
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

    fun <P, C : Any> setPersistentData(container: PersistentDataContainer, data: PersistentDataRecord<P, C>) {
        container.set(data.nameSpacedKey, data.persistentDataType, data.value)
    }


    companion object {
        fun builder() = Builder()
    }


    class Builder {
        private var player: Player? = null
        private var particle: Particle? = null
        private var particleData: Any? = null
        private var key: NamespacedKey? = null
        private var ticks: Long = 100
        private var hideRadius: Double = 65.0
        private var onTickCallback: ((Snowball) -> Unit)? = null
        private val spells: MutableList<PersistentDataRecord<*, *>> = mutableListOf()

        fun player(player: Player) = apply { this.player = player }
        fun particle(particle: Particle?) = apply { this.particle = particle }
        fun particleData(data: Any?) = apply { this.particleData = data }
        fun key(key: NamespacedKey) = apply { this.key = key }
        fun ticks(ticks: Long) = apply { this.ticks = ticks }
        fun hideRadius(radius: Double) = apply { this.hideRadius = radius }
        fun onTick(callback: (Snowball) -> Unit) = apply { this.onTickCallback = callback }
        fun addSpellData(record: PersistentDataRecord<*, *>) = apply { this.spells.add(record) }

        fun build(): SpellCaster {
            return SpellCaster(
                player ?: throw IllegalArgumentException("Player must be provided"),
                particle,
                particleData,
                key ?: throw IllegalArgumentException("Key must be provided"),
                ticks,
                hideRadius,
                onTickCallback,
                spells
            )
        }

        fun runNow(): SpellCaster {
            return build().also { it.cast() }
        }
    }
}