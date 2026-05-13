package dev.lumas.lumaitems.events.item

import com.gmail.nossr50.api.AbilityAPI as mcMMOAbilityAPI
import dev.lumas.lumacore.utility.Logging
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.hooks.McMMOHook
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.model.item.PdcSource
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.ServiceDeterrents
import dev.lumas.lumaitems.util.extensions.actionBar
import io.papermc.paper.persistence.PersistentDataContainerView
import java.util.EnumMap
import java.util.UUID
import java.util.WeakHashMap
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

abstract class ItemListener : Listener {

    companion object {

        // This exists because Kotlin doesn't allow null values unless the variable is nullable, and I'm not going to edit 75+ classes
        // Maybe replace with a class that implements player sometime?
        private var player: Player? = null
        // Loosely notify players if they're using a disabled custom item
        private val notifees: MutableSet<UUID> = mutableSetOf()
        private val reducedCalls: EnumMap<Action, Int> = EnumMap(Action::class.java)
        private val lastBreakSound: MutableMap<UUID, Long> = WeakHashMap()
        private const val BREAK_SOUND_COOLDOWN_MS = 1500L


        fun getDummyPlayer(): Player? {
            if (player == null && Bukkit.getOnlinePlayers().isNotEmpty()) {
                player = Bukkit.getOnlinePlayers().random()
            }
            return player
        }

        fun notify(player: Player, persistentNotification: Boolean) {
            if (notifees.contains(player.uniqueId) && !persistentNotification) return
            player.actionBar("<red>Custom abilities for equipped item(s) are disabled in this world.")
            notifees.add(player.uniqueId)
        }

        fun isHardDisabledAt(item: ItemStack?, location: Location): Boolean {
            val pdc = item?.itemMeta?.persistentDataContainer ?: return false
            for (entry in Registry.CUSTOM_ITEMS) {
                val customItem = entry.value
                if (pdc.has(entry.key.asNameSpacedKey()) && customItem.isDisabled(location) && customItem.isHardDisabled()) {
                    return true
                }
            }
            return false
        }
    }


    /**
     * Fires all matching custom items against a single PDC view.
     * Accepts both [PersistentDataContainerView] and [org.bukkit.persistence.PersistentDataContainer]
     * since the latter extends the former.
     */
    fun fire(
        source: PdcSource?,
        action: Action,
        player: Player?,
        event: Any,
        optimize: Boolean = false,
        withContainer: Boolean = false
    ) {

        if (!optimize && source?.isHealthTooLow() == true) {
            if (ServiceDeterrents.applyDeterrent(source.item, player, event, action) && player != null) {
                tryPlayBreakSound(player)
            }
            return
        }

        for ((registryKey, item) in Registry.CUSTOM_ITEMS) {
            if (!shouldFire(item, source?.data, registryKey.asNameSpacedKey(), action)) continue
            if (!action.isHot && handleDisabledIfNeeded(item, player, event, action)) return

            val effectivePlayer = player ?: getDummyPlayer() ?: return
            item.fireVerbosely(action, effectivePlayer, event, if (withContainer) source?.data else null)
        }
    }

    fun fire(
        data: List<PdcSource>,
        action: Action,
        player: Player?,
        event: Any,
        optimize: Boolean = false,
        withContainer: Boolean = false
    ) {
        val containers = data.ifEmpty { listOf(null) }
        for (itemData in containers) {
            fire(itemData, action, player, event, optimize, withContainer)
        }
    }

    // TODO: @FireAnyways
    fun fire(key: String, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for ((registryKey, item) in Registry.CUSTOM_ITEMS) {
            if (!key.equals(registryKey.asSimpleString(), true)) continue
            if (handleDisabledIfNeeded(item, player, event, action)) return

            val effectivePlayer = player ?: getDummyPlayer() ?: return
            val container = if (withContainer) player?.persistentDataContainer else null
            item.fireVerbosely(action, effectivePlayer, event, container)
            break
        }
    }


    /** Whether the given item should fire for this action, considering PDC presence and fire-anyways flag. */
    private fun shouldFire(
        item: CustomItem,
        data: PersistentDataContainerView?,
        key: NamespacedKey,
        action: Action
    ): Boolean {
        val hasKey = data?.has(key) == true
        return hasKey || item.fireAnyways(action)
    }

    /**
     * If the item is disabled at the player's location (and not flagged fire-anyways for this action),
     * runs [CustomItem.handleDisabled] and returns true. Otherwise returns false.
     */
    private fun handleDisabledIfNeeded(
        item: CustomItem,
        player: Player?,
        event: Any,
        action: Action
    ): Boolean {
        if (player == null) return false
        if (item.fireAnyways(action)) return false
        if (!item.isDisabled(player.location)) return false
        item.handleDisabled(player, event)
        return true
    }


    fun Action.canFireRightNow(): Boolean {
        if (this.callSlowdown < 1) return true

        // TODO: optimize and/or make this per player?
        val current = reducedCalls.getOrDefault(this, 0)
        return if (current >= this.callSlowdown) {
            reducedCalls[this] = 0
            true
        } else {
            reducedCalls[this] = current + 1
            false
        }
    }

    fun isTreeFeller(player: Player): Boolean {
        try {
            if (Registry.HOOKS.getOrThrow(McMMOHook::class).isWith() && mcMMOAbilityAPI.treeFellerEnabled(player)) {
                return true
            }
        } catch (throwable: Throwable) {
            Logging.errorLog("Error checking mcMMO tree feller state for player ${player.name}", throwable)
        }
        return false
    }

    private fun tryPlayBreakSound(player: Player) {
        val now = System.currentTimeMillis()
        val last = lastBreakSound[player.uniqueId] ?: 0L
        if (now - last < BREAK_SOUND_COOLDOWN_MS) return

        lastBreakSound[player.uniqueId] = now
        player.playSound(player.location, Sound.ENTITY_ITEM_BREAK, 0.35f, 1.2f)
    }

    private fun CustomItem.fireVerbosely(
        action: Action,
        player: Player,
        event: Any,
        container: PersistentDataContainerView? = null
    ) {
        try {
            if (container == null) {
                executeActions(action, player, event)
            } else {
                executeWithContainer(action, player, event, container)
            }
        } catch (throwable: Throwable) {
            Logging.errorLog("An error occurred while firing custom item '${this.javaClass.simpleName}' for player ${player.name} on action $action")
            throwable.printStackTrace()
        }
    }
}