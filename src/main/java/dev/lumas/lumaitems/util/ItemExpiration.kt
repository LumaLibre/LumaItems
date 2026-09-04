package dev.lumas.lumaitems.util

import dev.lumas.lumaitems.model.item.PdcSource
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.sync
import io.papermc.paper.persistence.PersistentDataContainerView
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType

object ItemExpiration {

    private val EXPIRES_AT = "expires_at".namespacedKey()
    private val EXPIRY_LORE_LINES = "expires_lore_lines".namespacedKey()
    private val TRACKED_SLOTS = arrayOf(
        EquipmentSlot.HAND,
        EquipmentSlot.OFF_HAND,
        EquipmentSlot.HEAD,
        EquipmentSlot.CHEST,
        EquipmentSlot.LEGS,
        EquipmentSlot.FEET
    )
    private const val NO_EXPIRY = Long.MIN_VALUE

    private const val SWEEP_PERIOD_TICKS: Long = 100
    private const val MAX_DURATION_SECONDS: Long = 3650L * 24 * 60 * 60 // 10 years
    private val DURATION_TOKEN = Regex("""(\d+)([wdhms])""")

    private val SERVER_ZONE: ZoneId = ZoneId.of("America/New_York")
    private val STAMP: DateTimeFormatter = DateTimeFormatter
        .ofPattern("MMM d 'at' HH:mm z")
        .withLocale(Locale.ENGLISH)
        .withZone(SERVER_ZONE)

    fun parseDuration(raw: String): Duration? {
        val cleaned = raw.lowercase(Locale.ENGLISH).filterNot { it == ',' || it.isWhitespace() }
        if (cleaned.isEmpty()) return null

        var seconds = 0L
        var index = 0
        for (match in DURATION_TOKEN.findAll(cleaned)) {
            if (match.range.first != index) return null
            index = match.range.last + 1

            val amount = match.groupValues[1].toLongOrNull() ?: return null
            val unit = when (match.groupValues[2]) {
                "w" -> 7L * 24 * 60 * 60
                "d" -> 24L * 60 * 60
                "h" -> 60L * 60
                "m" -> 60L
                else -> 1L
            }
            seconds = try {
                Math.addExact(seconds, Math.multiplyExact(amount, unit))
            } catch (_: ArithmeticException) {
                return null
            }
        }

        if (index != cleaned.length) return null
        if (seconds !in 1..MAX_DURATION_SECONDS) return null
        return Duration.ofSeconds(seconds)
    }

    fun formatSpan(millis: Long): String {
        var seconds = millis / 1000
        val days = seconds / 86400
        seconds %= 86400
        val hours = seconds / 3600
        seconds %= 3600
        val minutes = seconds / 60
        seconds %= 60

        val parts = mutableListOf<String>()
        if (days > 0) parts.add("${days}d")
        if (hours > 0) parts.add("${hours}h")
        if (minutes > 0) parts.add("${minutes}m")
        if (seconds > 0 || parts.isEmpty()) parts.add("${seconds}s")
        return parts.joinToString(" ")
    }

    fun formatStamp(epochMillis: Long): String = STAMP.format(Instant.ofEpochMilli(epochMillis))

    fun expiresAt(item: ItemStack?): Long? {
        if (item == null || item.type.isAir || !item.hasItemMeta()) return null
        return item.persistentDataContainer.get(EXPIRES_AT, PersistentDataType.LONG)
    }

    fun expiresAt(data: PersistentDataContainerView?): Long? {
        return data?.get(EXPIRES_AT, PersistentDataType.LONG)
    }

    fun isExpired(item: ItemStack?, now: Long = System.currentTimeMillis()): Boolean {
        return (expiresAt(item) ?: return false) <= now
    }

    // For items being transformed into a freshly built ItemStack
    fun transfer(from: ItemStack?, to: ItemStack): Boolean {
        val epochMillis = expiresAt(from) ?: return false
        forget(to)
        apply(to, epochMillis)
        return true
    }

    // Removes expiry tags without touching lore (for items whose lore was just rebuilt)
    private fun forget(item: ItemStack) {
        val meta = item.itemMeta ?: return
        meta.persistentDataContainer.remove(EXPIRES_AT)
        meta.persistentDataContainer.remove(EXPIRY_LORE_LINES)
        item.itemMeta = meta
    }

    fun apply(item: ItemStack, epochMillis: Long) {
        val meta = item.itemMeta ?: return
        stripLore(meta)

        val lines = loreLines(epochMillis)
        val lore = meta.lore()?.toMutableList() ?: mutableListOf()
        lore.addAll(lines.asComponent())
        meta.lore(lore)

        meta.persistentDataContainer.set(EXPIRES_AT, PersistentDataType.LONG, epochMillis)
        meta.persistentDataContainer.set(EXPIRY_LORE_LINES, PersistentDataType.INTEGER, lines.size)
        item.itemMeta = meta
    }

    fun clear(item: ItemStack): Boolean {
        if (expiresAt(item) == null) return false
        val meta = item.itemMeta ?: return false

        stripLore(meta)
        meta.persistentDataContainer.remove(EXPIRES_AT)
        item.itemMeta = meta
        return true
    }

    private fun loreLines(epochMillis: Long): List<String> = listOf(
        "",
        "<red>Expires ${formatStamp(epochMillis)}</red>",
        "<gray>Expired items are deleted</gray>"
    )

    fun appendedLoreLines(meta: ItemMeta?): Int {
        return meta?.persistentDataContainer?.get(EXPIRY_LORE_LINES, PersistentDataType.INTEGER) ?: 0
    }

    private fun stripLore(meta: ItemMeta) {
        val count = meta.persistentDataContainer.get(EXPIRY_LORE_LINES, PersistentDataType.INTEGER) ?: return
        meta.persistentDataContainer.remove(EXPIRY_LORE_LINES)

        val lore = meta.lore()?.toMutableList() ?: return
        repeat(minOf(count, lore.size)) { lore.removeAt(lore.size - 1) }
        meta.lore(lore.ifEmpty { null })
    }

    fun snapshot(player: Player?, source: PdcSource?): SlotSnapshot? {
        if (player == null || expiresAt(source?.data) == null) return null

        val inventory = player.inventory
        val before = arrayOfNulls<ItemStack>(TRACKED_SLOTS.size)
        val expiries = LongArray(TRACKED_SLOTS.size) { NO_EXPIRY }

        var tracking = false
        for (index in TRACKED_SLOTS.indices) {
            val item = inventory.getItem(TRACKED_SLOTS[index])
            if (item.type.isAir) continue

            before[index] = item
            expiries[index] = expiresAt(item) ?: continue
            tracking = true
        }
        return if (tracking) SlotSnapshot(inventory, before, expiries) else null
    }

    class SlotSnapshot internal constructor(
        private val inventory: PlayerInventory,
        private val before: Array<ItemStack?>,
        private val expiries: LongArray
    ) {

        fun restore() {
            for (index in TRACKED_SLOTS.indices) {
                val epochMillis = expiries[index]
                if (epochMillis == NO_EXPIRY) continue

                val slot = TRACKED_SLOTS[index]
                val now = inventory.getItem(slot)
                if (now.type.isAir) continue
                if (expiresAt(now) != null) continue // The tag survived the handler, or the handler set an expiry of its own
                if (before.any { it != null && it.isSimilar(now) }) continue

                apply(now, epochMillis)
                inventory.setItem(slot, now)
            }
        }
    }

    @JvmStatic
    fun startSweepTask(): ScheduledTask {
        return Executors.asyncTimer(SWEEP_PERIOD_TICKS, SWEEP_PERIOD_TICKS) {
            val now = System.currentTimeMillis()
            for (player in Bukkit.getOnlinePlayers()) {
                player.sync { sweep(player, now) }
            }
        }
    }

    fun sweep(player: Player, now: Long = System.currentTimeMillis()): Int {
        var removed = sweep(player.inventory, now) + sweep(player.enderChest, now)

        val cursor = player.itemOnCursor
        if (isExpired(cursor, now)) {
            removed += cursor.amount
            player.setItemOnCursor(null)
        }

        notifyRemoved(player, removed)
        return removed
    }

    fun notifyRemoved(player: Player, removed: Int) {
        if (removed <= 0) return
        val noun = if (removed == 1) "item has" else "items have"
        player.send("$removed expired $noun been removed")
    }

    // Must run on the inventory's thread
    fun sweep(inventory: Inventory, now: Long = System.currentTimeMillis()): Int {
        var removed = 0
        val contents = inventory.contents
        for (slot in contents.indices) {
            val item = contents[slot] ?: continue
            if (!isExpired(item, now)) continue
            inventory.setItem(slot, null)
            removed += item.amount
        }
        return removed
    }
}
