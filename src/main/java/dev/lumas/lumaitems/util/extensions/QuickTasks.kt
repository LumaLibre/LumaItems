@file:JvmName("JvmQuickTasks")

package dev.lumas.lumaitems.util.extensions

import dev.lumas.lumaitems.model.AbstractSpell
import dev.lumas.lumaitems.model.CustomItem
import dev.lumas.lumaitems.model.CustomItemCooldown
import dev.lumas.lumaitems.model.SpellCooldown
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.entity.Player


private val ACTIVE_COOLDOWNS: MutableMap<Class<out CustomItem>, CustomItemCooldown> = ConcurrentHashMap()
private val ACTIVE_FLAGS: MutableMap<Class<out CustomItem>, MutableMap<UUID, Any?>> = ConcurrentHashMap()
private val ACTIVE_SPELL_COOLDOWNS: MutableSet<SpellCooldown<*>> = ConcurrentHashMap.newKeySet()

private fun CustomItem.getOrCreateCooldown(): CustomItemCooldown {
    return ACTIVE_COOLDOWNS.getOrPut(this::class.java) {
        CustomItemCooldown(this::class.java, mutableListOf())
    }
}

// ======
// Standard cooldowns
// ======

fun UUID.isOnCooldown(customItem: CustomItem): Boolean {
    return ACTIVE_COOLDOWNS[customItem::class.java]?.isOnCooldown(this) ?: false
}


fun Player.isOnCooldown(customItem: CustomItem): Boolean {
    return uniqueId.isOnCooldown(customItem)
}

fun UUID.addCooldown(customItem: CustomItem, ticks: Long) {
    val cooldown = customItem.getOrCreateCooldown()
    cooldown.addCooldown(this)
    Executors.asyncDelayed(ticks) { cooldown.removeCooldown(this) }
}

fun Player.addCooldown(customItem: CustomItem, ticks: Long) {
    uniqueId.addCooldown(customItem, ticks)
}


fun UUID.addCooldown(customItem: CustomItem, ticks: Long, callback: () -> Unit) {
    val cooldown = customItem.getOrCreateCooldown()
    cooldown.addCooldown(this)
    Executors.asyncDelayed(ticks) {
        cooldown.removeCooldown(this)
        callback()
    }
}

fun Player.addCooldown(customItem: CustomItem, ticks: Long, callback: () -> Unit) {
    uniqueId.addCooldown(customItem, ticks, callback)
}

fun CustomItem.getActiveCooldowns(): Int {
    return ACTIVE_COOLDOWNS[this::class.java]?.players?.size ?: 0
}

fun UUID.addCooldownIndefinitely(customItem: CustomItem) {
    customItem.getOrCreateCooldown().addCooldown(this)
}

fun Player.addCooldownIndefinitely(customItem: CustomItem) {
    uniqueId.addCooldownIndefinitely(customItem)
}

fun UUID.removeNow() {
    ACTIVE_COOLDOWNS.values.forEach { it.removeCooldown(this) }
}

fun Player.removeNow() {
    uniqueId.removeNow()
}

fun UUID.removeNow(customItem: CustomItem) {
    ACTIVE_COOLDOWNS[customItem::class.java]?.removeCooldown(this)
}

fun Player.removeNow(customItem: CustomItem) {
    uniqueId.removeNow(customItem)
}

fun UUID.removeWhen(customItem: CustomItem, ticks: Long) {
    Executors.asyncDelayed(ticks) { removeNow(customItem) }
}

fun Player.removeWhen(customItem: CustomItem, ticks: Long) {
    uniqueId.removeWhen(customItem, ticks)
}

// ======
// Flags
// ======

fun UUID.flag(customItem: CustomItem, value: Any? = null) {
    ACTIVE_FLAGS.getOrPut(customItem::class.java) { HashMap() }[this] = value
}

fun Player.flag(customItem: CustomItem, value: Any? = null) {
    uniqueId.flag(customItem, value)
}

fun UUID.flagFor(customItem: CustomItem, ticks: Long, value: Any? = null) {
    flag(customItem, value)
    Executors.asyncDelayed(ticks) { removeFlag(customItem) }
}

fun Player.flagFor(customItem: CustomItem, ticks: Long, value: Any? = null) {
    uniqueId.flagFor(customItem, ticks, value)
}

fun UUID.isFlagged(customItem: CustomItem): Boolean {
    return ACTIVE_FLAGS[customItem::class.java]?.containsKey(this) ?: false
}

fun Player.isFlagged(customItem: CustomItem): Boolean {
    return uniqueId.isFlagged(customItem)
}


fun UUID.getFlag(customItem: CustomItem): Any? {
    return ACTIVE_FLAGS[customItem::class.java]?.get(this)
}

fun Player.getFlag(customItem: CustomItem): Any? {
    return uniqueId.getFlag(customItem)
}

@Suppress("UNCHECKED_CAST")
fun <T> UUID.getFlag(customItem: CustomItem, expected: Class<T>): T? {
    val value = getFlag(customItem) ?: return null
    val boxed = when (expected) {
        Double::class.javaPrimitiveType -> java.lang.Double::class.java
        Int::class.javaPrimitiveType -> Integer::class.java
        Long::class.javaPrimitiveType -> java.lang.Long::class.java
        Float::class.javaPrimitiveType -> java.lang.Float::class.java
        Boolean::class.javaPrimitiveType -> java.lang.Boolean::class.java
        else -> expected
    }
    return if (boxed.isInstance(value)) value as T else null
}

fun <T> Player.getFlag(customItem: CustomItem, expected: Class<T>): T? {
    return uniqueId.getFlag(customItem, expected)
}

@JvmName("getTypedFlag")
inline fun <reified T> UUID.getFlag(customItem: CustomItem): T? {
    return getFlag(customItem, T::class.java)
}

@JvmName("getTypedFlag")
inline fun <reified T> Player.getFlag(customItem: CustomItem): T? {
    return uniqueId.getFlag(customItem, T::class.java)
}

fun UUID.removeFlag(customItem: CustomItem) {
    ACTIVE_FLAGS[customItem::class.java]?.remove(this)
}

fun Player.removeFlag(customItem: CustomItem) {
    uniqueId.removeFlag(customItem)
}

fun UUID.removeAllFlags() {
    ACTIVE_FLAGS.values.forEach { it.remove(this) }
}

fun Player.removeAllFlags() {
    uniqueId.removeAllFlags()
}

fun CustomItem.clearFlags() {
    ACTIVE_FLAGS.remove(this::class.java)
}

// ======
// Spell cooldowns
// ======

fun <T> UUID.isOnSpellCooldown(spell: T): Boolean where T : Enum<T>, T : AbstractSpell {
    return ACTIVE_SPELL_COOLDOWNS.contains(SpellCooldown.of(this, spell))
}

fun <T> Player.isOnSpellCooldown(spell: T): Boolean where T : Enum<T>, T : AbstractSpell {
    return uniqueId.isOnSpellCooldown(spell)
}

fun <T> UUID.addSpellCooldown(spell: T) where T : Enum<T>, T : AbstractSpell {
    val cooldown = SpellCooldown.of(this, spell)
    ACTIVE_SPELL_COOLDOWNS.add(cooldown)
    Executors.asyncDelayed(spell.cooldown) {
        ACTIVE_SPELL_COOLDOWNS.remove(cooldown)
    }
}

fun <T> Player.addSpellCooldown(spell: T) where T : Enum<T>, T : AbstractSpell {
    uniqueId.addSpellCooldown(spell)
}

fun <T> UUID.addSpellCooldown(spell: T, ticks: Long) where T : Enum<T>, T : AbstractSpell {
    val cooldown = SpellCooldown.of(this, spell)
    ACTIVE_SPELL_COOLDOWNS.add(cooldown)
    Executors.asyncDelayed(ticks) {
        ACTIVE_SPELL_COOLDOWNS.remove(cooldown)
    }
}

fun <T> Player.addSpellCooldown(spell: T, ticks: Long) where T : Enum<T>, T : AbstractSpell {
    uniqueId.addSpellCooldown(spell, ticks)
}

fun <T> UUID.removeSpellCooldown(spell: T) where T : Enum<T>, T : AbstractSpell {
    ACTIVE_SPELL_COOLDOWNS.remove(SpellCooldown.of(this, spell))
}

fun <T> Player.removeSpellCooldown(spell: T) where T : Enum<T>, T : AbstractSpell {
    uniqueId.removeSpellCooldown(spell)
}

fun UUID.removeAllSpellCooldowns() {
    ACTIVE_SPELL_COOLDOWNS.removeAll { it.uuid == this }
}

fun Player.removeAllSpellCooldowns() {
    uniqueId.removeAllSpellCooldowns()
}


object QuickTasks {

    fun isOnCooldown(customItem: CustomItem, uuid: UUID) = uuid.isOnCooldown(customItem)
    fun isOnCooldown(customItem: CustomItem, player: Player) = player.isOnCooldown(customItem)

    fun addCooldown(customItem: CustomItem, player: Player, ticks: Long) = player.addCooldown(customItem, ticks)
    fun addCooldown(customItem: CustomItem, player: Player, ticks: Long, callback: () -> Unit) = player.addCooldown(customItem, ticks, callback)
    fun addCooldown(customItem: CustomItem, uuid: UUID, ticks: Long) = uuid.addCooldown(customItem, ticks)
    fun addCooldown(customItem: CustomItem, uuid: UUID, ticks: Long, callback: () -> Unit) = uuid.addCooldown(customItem, ticks, callback)

    fun getActiveCooldowns(customItem: CustomItem) = customItem.getActiveCooldowns()

    fun addCooldownIndefinitely(customItem: CustomItem, player: UUID) = player.addCooldownIndefinitely(customItem)

    fun removeNow(uuid: UUID) = uuid.removeNow()
    fun removeNow(customItem: CustomItem, uuid: UUID) = uuid.removeNow(customItem)

    fun removeWhen(customItem: CustomItem, uuid: UUID, ticks: Long) = uuid.removeWhen(customItem, ticks)

    // Flags

    fun flag(customItem: CustomItem, uuid: UUID, value: Any? = null) = uuid.flag(customItem, value)
    fun flag(customItem: CustomItem, uuid: Player, value: Any? = null)  = uuid.flag(customItem, value)

    fun flagFor(customItem: CustomItem, uuid: UUID, ticks: Long, value: Any? = null) = uuid.flagFor(customItem, ticks, value)

    fun isFlagged(customItem: CustomItem, uuid: UUID) = uuid.isFlagged(customItem)
    fun isFlagged(customItem: CustomItem, player: Player) = player.isFlagged(customItem)


    fun getFlag(customItem: CustomItem, uuid: UUID): Any? = uuid.getFlag(customItem)
    fun <T> getFlag(customItem: CustomItem, uuid: UUID, expected: Class<T>) = uuid.getFlag(customItem, expected)
    @JvmName("getTypedFlag")
    inline fun <reified T> getFlag(customItem: CustomItem, uuid: UUID) = uuid.getFlag(customItem, T::class.java)

    fun removeFlag(customItem: CustomItem, uuid: UUID) = uuid.removeFlag(customItem)

    fun removeFlag(customItem: CustomItem, player: Player) = player.removeFlag(customItem)

    fun removeAllFlags(uuid: UUID) = uuid.removeAllFlags()

    fun clearFlags(customItem: CustomItem) = customItem.clearFlags()

    // Spell cooldowns

    fun <T> isOnSpellCooldown(uuid: UUID, spell: T): Boolean where T : Enum<T>, T : AbstractSpell = uuid.isOnSpellCooldown(spell)
    fun <T> isOnSpellCooldown(player: Player, spell: T): Boolean where T : Enum<T>, T : AbstractSpell = player.isOnSpellCooldown(spell)

    fun <T> addSpellCooldown(uuid: UUID, spell: T) where T : Enum<T>, T : AbstractSpell = uuid.addSpellCooldown(spell)

    fun <T> addSpellCooldown(player: Player, spell: T) where T : Enum<T>, T : AbstractSpell = player.addSpellCooldown(spell)

    fun <T> addSpellCooldown(uuid: UUID, spell: T, ticks: Long) where T : Enum<T>, T : AbstractSpell = uuid.addSpellCooldown(spell, ticks)

    fun <T> removeSpellCooldown(uuid: UUID, spell: T) where T : Enum<T>, T : AbstractSpell  = uuid.removeSpellCooldown(spell)
    fun <T> removeSpellCooldown(player: Player, spell: T) where T : Enum<T>, T : AbstractSpell = player.removeSpellCooldown(spell)

    fun removeAllSpellCooldowns(uuid: UUID) = uuid.removeAllSpellCooldowns()
}