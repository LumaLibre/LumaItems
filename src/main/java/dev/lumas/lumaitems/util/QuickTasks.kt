package dev.lumas.lumaitems.util

import dev.lumas.lumacore.utility.ContextLogger
import dev.lumas.lumaitems.model.CustomItem
import dev.lumas.lumaitems.model.CustomItemCooldown
import dev.lumas.lumaitems.util.extensions.Executors
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.entity.Player

object QuickTasks {

    private val LOGGER = ContextLogger.getLogger(true)

    val activeCooldowns: MutableMap<Class<out CustomItem>, CustomItemCooldown> = ConcurrentHashMap()
    val flags: MutableMap<Class<out CustomItem>, MutableMap<UUID, Any?>> = ConcurrentHashMap()


    @JvmStatic
    fun isOnCooldown(customItem: CustomItem, player: UUID): Boolean {
        return activeCooldowns[customItem::class.java]?.isOnCooldown(player) ?: false
    }

    @JvmStatic
    fun isOnCooldown(customItem: CustomItem, player: Player): Boolean {
        return isOnCooldown(customItem, player.uniqueId)
    }

    @JvmStatic
    fun addCooldown(customItem: CustomItem, player: Player, ticks: Long) {
        addCooldown(customItem, player.uniqueId, ticks)
    }

    @JvmStatic
    fun addCooldown(customItem: CustomItem, player: Player, ticks: Long, callback: () -> Unit) {
        addCooldown(customItem, player.uniqueId, ticks, callback)
    }

    @JvmStatic
    fun addCooldown(customItem: CustomItem, player: UUID, ticks: Long) {
        val cooldown = getOrCreateCooldown(customItem)
        cooldown.addCooldown(player)
        Executors.asyncDelayed(ticks) { cooldown.removeCooldown(player) }
    }

    @JvmStatic
    fun addCooldown(customItem: CustomItem, player: UUID, ticks: Long, callback: () -> Unit) {
        val cooldown = getOrCreateCooldown(customItem)
        cooldown.addCooldown(player)
        Executors.asyncDelayed(ticks) {
            cooldown.removeCooldown(player)
            callback()
        }
    }

    @JvmStatic
    fun getActiveCooldowns(customItem: CustomItem): Int {
        return activeCooldowns[customItem::class.java]?.players?.size ?: 0
    }

    fun addCooldownIndefinitely(customItem: CustomItem, player: UUID) {
        getOrCreateCooldown(customItem).addCooldown(player)
    }

    fun removeNow(player: UUID) {
        activeCooldowns.values.forEach { it.removeCooldown(player) }
    }

    fun removeNow(customItem: CustomItem, player: UUID) {
        activeCooldowns[customItem::class.java]?.removeCooldown(player)
    }

    fun removeWhen(customItem: CustomItem, player: UUID, ticks: Long) {
        Executors.asyncDelayed(ticks) { removeNow(customItem, player) }
    }

    private fun getOrCreateCooldown(customItem: CustomItem): CustomItemCooldown {
        return activeCooldowns.getOrPut(customItem::class.java) {
            CustomItemCooldown(customItem::class.java, mutableListOf())
        }
    }


    fun flag(customItem: CustomItem, player: UUID, value: Any? = null) {
        flags.getOrPut(customItem::class.java) { HashMap() }[player] = value
    }

    fun flag(customItem: CustomItem, player: Player, value: Any? = null) {
        flag(customItem, player.uniqueId, value)
    }

    fun flagFor(customItem: CustomItem, player: UUID, ticks: Long, value: Any? = null) {
        flag(customItem, player, value)
        Executors.asyncDelayed(ticks) { removeFlag(customItem, player) }
    }


    fun isFlagged(customItem: CustomItem, player: UUID): Boolean {
        return flags[customItem::class.java]?.containsKey(player) ?: false
    }

    fun isFlagged(customItem: CustomItem, player: Player): Boolean {
        return isFlagged(customItem, player.uniqueId)
    }


    fun getFlag(customItem: CustomItem, player: UUID): Any? {
        return flags[customItem::class.java]?.get(player)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> getFlag(customItem: CustomItem, player: UUID, expected: Class<T>): T? {
        val value = getFlag(customItem, player) ?: return null
        val boxed = when (expected) {
            Double::class.javaPrimitiveType -> java.lang.Double::class.java
            Int::class.javaPrimitiveType -> java.lang.Integer::class.java
            Long::class.javaPrimitiveType -> java.lang.Long::class.java
            Float::class.javaPrimitiveType -> java.lang.Float::class.java
            Boolean::class.javaPrimitiveType -> java.lang.Boolean::class.java
            else -> expected
        }
        return if (boxed.isInstance(value)) value as T else null
    }


    @JvmName("getTypedFlag")
    inline fun <reified T> getFlag(customItem: CustomItem, player: UUID): T? {
        return getFlag(customItem, player, T::class.java)
    }

    /**
     * Remove a player's flag for a specific item.
     */
    fun removeFlag(customItem: CustomItem, player: UUID) {
        flags[customItem::class.java]?.remove(player)
    }

    fun removeFlag(customItem: CustomItem, player: Player) {
        removeFlag(customItem, player.uniqueId)
    }

    fun removeAllFlags(player: UUID) {
        flags.values.forEach { it.remove(player) }
    }

    fun clearFlags(customItem: CustomItem) {
        flags.remove(customItem::class.java)
    }
}