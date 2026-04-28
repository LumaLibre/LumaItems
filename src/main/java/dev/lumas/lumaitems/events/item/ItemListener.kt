package dev.lumas.lumaitems.events.item

import com.gmail.nossr50.api.AbilityAPI as mcMMOAbilityAPI
import dev.lumas.lumacore.utility.Logging
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.hooks.McMMOHook
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.actionBar
import io.papermc.paper.persistence.PersistentDataContainerView
import java.util.EnumMap
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataContainer

// TODO: cleanup this class & extract common code
abstract class ItemListener : Listener {

    companion object {

        // This exists because Kotlin doesn't allow null values unless the variable is nullable, and I'm not going to edit 75+ classes
        // Maybe replace with a class that implements player sometime?
        private var player: Player? = null
        // Loosely notify players if they're using a disabled custom item
        private val notifees: MutableSet<UUID> = mutableSetOf()
        private val reducedCalls: EnumMap<Action, Int> = EnumMap(Action::class.java)


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


    // Paper added this, just makes it easier to look at the PDC
    fun fire(data: PersistentDataContainerView, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (customItem in Registry.CUSTOM_ITEMS) {
            val item = customItem.value
            val fireAnyways = item.fireAnyways(action)

            if (!data.has(customItem.key.asNameSpacedKey()) && !fireAnyways) {
                continue
            }

            if (player?.location?.let { item.isDisabled(it) } == true) {
                item.handleDisabled(player, event)
                return
            }
            item.fireViewVerbosely(action, player ?: getDummyPlayer() ?: return, event, if (withContainer) data else null)
        }
    }

    fun fire(data: PersistentDataContainer, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (customItem in Registry.CUSTOM_ITEMS) {
            val item = customItem.value
            val fireAnyways = customItem.value.fireAnyways(action)

            if (!data.has(customItem.key.asNameSpacedKey()) && !fireAnyways) {
                continue
            }

            if (player?.location?.let { item.isDisabled(it) } == true && !fireAnyways) {
                item.handleDisabled(player, event)
                return
            }

            item.fireVerbosely(action, player ?: getDummyPlayer() ?: return, event, if (withContainer) data else null)
        }
    }

    fun fire(data: List<PersistentDataContainer>, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (itemData: PersistentDataContainer? in data.ifEmpty { listOf(null) }) {
            for (customItem in Registry.CUSTOM_ITEMS) {
                val fireAnyways = customItem.value.fireAnyways(action)

                if (!((itemData != null && itemData.has(customItem.key.asNameSpacedKey())) || fireAnyways)) {
                    continue
                }

                val item = customItem.value
                if (player?.location?.let { item.isDisabled(it) } == true && !fireAnyways) {
                    item.handleDisabled(player, event)
                    break
                }

                item.fireVerbosely(action, player ?: getDummyPlayer() ?: return, event, if (withContainer) itemData else null)
            }
        }
    }

    // TODO: @FireAnyways
    fun fire(key: String, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (customItem in Registry.CUSTOM_ITEMS) {
            if (key.equals(customItem.key.asSimpleString(), true)) {
                val item = customItem.value
                if (player?.location?.let { item.isDisabled(it) } == true) {
                    item.handleDisabled(player, event)
                    return
                }

                item.fireVerbosely(action, player ?: getDummyPlayer() ?: return, event, if (withContainer) player?.persistentDataContainer else null)
                break
            }
        }
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

    private fun CustomItem.fireVerbosely(action: Action, player: Player, event: Any, container: PersistentDataContainer? = null) {
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

    private fun CustomItem.fireViewVerbosely(action: Action, player: Player, event: Any, container: PersistentDataContainerView? = null) {
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