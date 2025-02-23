package dev.jsinco.luma.lumaitems.events.items

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.manager.ItemManager
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import io.papermc.paper.persistence.PersistentDataContainerView
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import java.util.UUID

@Suppress("Duplicates")
abstract class ItemListener : Listener {

    companion object {
        // This exists because Kotlin doesn't allow null values unless the variable is nullable, and I'm not going to edit 75+ classes
        // Maybe replace with a class that implements player sometime?
        private var player: Player? = null
        fun getDummyPlayer(): Player? {
            if (player == null && Bukkit.getOnlinePlayers().isNotEmpty()) {
                player = Bukkit.getOnlinePlayers().random()
            }
            return player
        }


        // Loosely notify players if they're using a disabled custom item
        private val notifees: MutableSet<UUID> = mutableSetOf()
        fun notify(player: Player, persistentNotification: Boolean) {
            if (notifees.contains(player.uniqueId) && !persistentNotification) return
            player.sendActionBar(MiniMessageUtil.mm("<red>Custom abilities for equipped item(s) are disabled in this world."))
            notifees.add(player.uniqueId)
        }
    }

    // Paper added this, just makes it easier to look at the PDC
    fun fire(data: PersistentDataContainerView, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (customItem: MutableMap.MutableEntry<NamespacedKey, CustomItem> in ItemManager.customItems) {
            if (!data.has(customItem.key, PersistentDataType.SHORT)) {
                continue
            }
            val item = customItem.value
            if (player?.location?.let { item.isDisabled(it) } == true) {
                item.handleDisabled(player, event)
                return
            }
            if (!withContainer) {
                item.executeActions(action, player ?: getDummyPlayer() ?: return, event)
            } else {
                item.executeWithContainer(action, player ?: getDummyPlayer() ?: return, event, data)
            }
            break
        }
    }

    fun fire(data: PersistentDataContainer, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (customItem: MutableMap.MutableEntry<NamespacedKey, CustomItem> in ItemManager.customItems) {
            if (!data.has(customItem.key, PersistentDataType.SHORT)) {
                continue
            }
            val item = customItem.value
            if (player?.location?.let { item.isDisabled(it) } == true) {
                item.handleDisabled(player, event)
                return
            }
            if (!withContainer) {
                item.executeActions(action, player ?: getDummyPlayer() ?: return, event)
            } else {
                item.executeWithContainer(action, player ?: getDummyPlayer() ?: return, event, data)
            }
            break
        }
    }

    fun fire(data: List<PersistentDataContainer>, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (itemData: PersistentDataContainer in data) {
            for (customItem: MutableMap.MutableEntry<NamespacedKey, CustomItem> in ItemManager.customItems) {
                if (!itemData.has(customItem.key, PersistentDataType.SHORT)) {
                    continue
                }
                val item = customItem.value
                if (player?.location?.let { item.isDisabled(it) } == true) {
                    item.handleDisabled(player, event)
                    break
                }
                if (!withContainer) {
                    item.executeActions(action, player ?: getDummyPlayer() ?: return, event)
                } else {
                    item.executeWithContainer(action, player ?: getDummyPlayer() ?: return, event, itemData)
                }
                break
            }
        }
    }

    fun fire(data: List<PersistentDataContainer>, action1: Action, action2: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (itemData: PersistentDataContainer in data) {
            for (customItem: MutableMap.MutableEntry<NamespacedKey, CustomItem> in ItemManager.customItems) {
                if (!itemData.has(customItem.key, PersistentDataType.SHORT)) {
                    continue
                }
                val item = customItem.value
                if (player?.location?.let { item.isDisabled(it) } == true) {
                    item.handleDisabled(player, event)
                    break
                }
                if (!withContainer) {
                    item.executeActions(action1, player ?: getDummyPlayer() ?: return, event)
                    item.executeActions(action2, player ?: getDummyPlayer() ?: return, event)
                } else {
                    item.executeWithContainer(action1, player ?: getDummyPlayer() ?: return, event, itemData)
                    item.executeWithContainer(action2, player ?: getDummyPlayer() ?: return, event, itemData)
                }
                break
            }
        }
    }

    fun fire(data: List<PersistentDataContainer>, vararg actions: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (itemData: PersistentDataContainer in data) {
            for (customItem: MutableMap.MutableEntry<NamespacedKey, CustomItem> in ItemManager.customItems) {
                if (!itemData.has(customItem.key, PersistentDataType.SHORT)) {
                    continue
                }
                val item = customItem.value
                if (player?.location?.let { item.isDisabled(it) } == true) {
                    item.handleDisabled(player, event)
                    break
                }
                for (action in actions) {
                    if (!withContainer) {
                        item.executeActions(action, player ?: getDummyPlayer() ?: return, event)
                    } else {
                        item.executeWithContainer(action, player ?: getDummyPlayer() ?: return, event, itemData)
                    }
                }
                break
            }
        }
    }

    fun fire(key: String, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (customItem in ItemManager.customItems) {
            if (key.equals(customItem.key.key, true)) {
                val item = customItem.value
                if (player?.location?.let { item.isDisabled(it) } == true) {
                    item.handleDisabled(player, event)
                    return
                }
                if (!withContainer) {
                    item.executeActions(action, player ?: getDummyPlayer() ?: return, event)
                } else {
                    item.executeWithContainer(action, player ?: getDummyPlayer() ?: return, event, player?.persistentDataContainer ?: return)
                }
                break
            }
        }
    }
}