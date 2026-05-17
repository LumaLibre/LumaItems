package dev.lumas.lumaitems.items.misc.nests

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.PersistentDataRecord
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.addHealth
import dev.lumas.lumaitems.util.extensions.getPersistentKey
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.syncTimer
import kotlin.math.cos
import kotlin.math.sin
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType

abstract class RepairToken : CustomItemFunctions() {

    private companion object {
        private val KEY_REPAIR_AMOUNT = "repair-amount".namespacedKey()
    }

    abstract val key: NamespacedKey

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><yellow>Repair Gem <!b><gray>(Tier ${tier()})")
            .lore(
                "<gray>Increase an items durability by <white>${repairAmount()}",
                "<gray>Drag this onto another item to repair it!"
            )
            .material(Material.FIREWORK_STAR)
            .vanillaEnchants(Enchantment.UNBREAKING to 1)
            .hideEnchants(true)
            .addSpace(false)
            .maxStackSize(72)
            .tier(Tier.BLANK)
            .persistentData(key)
            .persistentDataRecords(
                PersistentDataRecord.create(KEY_REPAIR_AMOUNT, PersistentDataType.INTEGER, repairAmount())
            )
            .buildPair()
    }

    override fun onInventoryClick(player: Player, event: InventoryClickEvent) {
        if (!event.click.isMouseClick) {
            return
        }

        val cursor = event.cursor.takeIf { it.isMatchingItem(key) } ?: return
        val clickedItem = event.currentItem ?: return
        val clickedItemMeta = clickedItem.itemMeta as? Damageable ?: run {
            if (clickedItem.type != Material.AIR) player.send("<red>This item cannot be repaired!")
            return
        }

        if (player.gameMode == GameMode.CREATIVE) {
            player.send("<red>Repairing items is disabled in creative mode!")
            return
        }

        if (!clickedItemMeta.hasDamage()) {
            player.send("<red>This item is already fully repaired!")
            return
        }

        event.isCancelled = true

        val repairAmount = cursor.getPersistentKey(KEY_REPAIR_AMOUNT, PersistentDataType.INTEGER) ?: 1

        clickedItem.addHealth(repairAmount)
        cursor.amount -= 1
        player.send("<green>This item has been repaired by <red>$repairAmount</red> uses!")

        player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f)
        spawnParticles(player)
    }

    // Copied from RepairTokens
    private fun spawnParticles(player: Player) {
        val locations = generateHelix(player)
        player.syncTimer(1, 1) { task ->
            var i = 0
            while (i < locations.size && i != 7) {
                val loc = locations[i]
                locations.removeAt(i)
                loc.getWorld().spawnParticle(Particle.CLOUD, loc, 1, 0.0, 0.0, 0.0, 0.0)
                i++
            }
            if (locations.isEmpty()) {
                task.cancel()
            }
        }
    }

    // Copied from RepairTokens
    private fun generateHelix(player: Player): MutableList<Location> {
        val locations = mutableListOf<Location>()
        val center = player.location

        var var3 = 0.0
        while (var3 <= 10.0) {
            val var5 = 1.5 * cos(var3)
            val var7 = 1.5 * sin(var3)
            locations.add(Location(center.world, center.x + var5, center.y + var3 / 4.0, center.z + var7))
            var3 += 0.1
        }

        return locations
    }

    abstract fun tier(): String
    abstract fun repairAmount(): Int
}

class RepairTokenTier1Item : RepairToken() {
    override val key: NamespacedKey = "repair-token-1".namespacedKey()
    override fun tier(): String = "I"
    override fun repairAmount(): Int = 150
    override fun tabCompleteName(): String = "repair_token_tier_1"
}

class RepairTokenTier2Item : RepairToken() {
    override val key: NamespacedKey = "repair-token-2".namespacedKey()
    override fun tier(): String = "II"
    override fun repairAmount(): Int = 250
    override fun tabCompleteName(): String = "repair_token_tier_2"
}

class RepairTokenTier3Item : RepairToken() {
    override val key: NamespacedKey = "repair-token-3".namespacedKey()
    override fun tier(): String = "III"
    override fun repairAmount(): Int = 450
    override fun tabCompleteName(): String = "repair_token_tier_3"
}