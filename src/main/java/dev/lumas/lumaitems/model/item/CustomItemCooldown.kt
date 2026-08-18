package dev.lumas.lumaitems.model.item

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

class CustomItemCooldown(
    val customItem: Class<out CustomItem>,
) {

    companion object {
        const val INDEFINITE = Long.MAX_VALUE
        private const val MILLIS_PER_TICK = 50L
    }

    private val expiries: MutableMap<UUID, Long> = ConcurrentHashMap()

    val players: Set<UUID>
        get() = expiries.keys

    fun isOnCooldown(player: UUID): Boolean {
        val expiry = expiries[player] ?: return false
        if (expiry == INDEFINITE || expiry > System.currentTimeMillis()) return true

        expiries.remove(player, expiry)
        return false
    }

    fun remainingTicks(player: UUID): Long {
        if (!isOnCooldown(player)) return 0
        val expiry = expiries[player] ?: return 0
        if (expiry == INDEFINITE) return INDEFINITE

        val left = expiry - System.currentTimeMillis()
        return if (left <= 0) 0 else (left + (MILLIS_PER_TICK - 1)) / MILLIS_PER_TICK
    }

    fun addCooldown(player: UUID) {
        expiries[player] = INDEFINITE
    }

    fun addCooldown(player: UUID, ticks: Long) {
        expiries[player] = System.currentTimeMillis() + (ticks * MILLIS_PER_TICK)
    }

    fun removeCooldown(player: UUID) {
        expiries.remove(player)
    }
}
