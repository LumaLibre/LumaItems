package dev.jsinco.luma.lumaitems.events.items

import com.gmail.nossr50.api.AbilityAPI as mcMMOAbilityAPI
import dev.jsinco.luma.lumacore.utility.Logging
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.manager.ItemManager
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import io.papermc.paper.persistence.PersistentDataContainerView
import java.util.UUID
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.persistence.PersistentDataContainer

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


        for (customItem: MutableMap.MutableEntry<NamespacedKey, CustomItem> in ItemManager.CUSTOM_ITEMS) {
            val item = customItem.value
            val fireAnyways = item.fireAnyways(action)

            if (!data.has(customItem.key) && !fireAnyways) {
                continue
            }

            if (player?.location?.let { item.isDisabled(it) } == true) {
                item.handleDisabled(player, event)
                return
            }
            if (!withContainer) {
                item.executeActions(action, player ?: getDummyPlayer() ?: return, event)
            } else {
                item.executeWithContainer(action, player ?: getDummyPlayer() ?: return, event, data)
            }
        }
    }

    fun fire(data: PersistentDataContainer, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (customItem: MutableMap.MutableEntry<NamespacedKey, CustomItem> in ItemManager.CUSTOM_ITEMS) {
            val item = customItem.value
            val fireAnyways = customItem.value.fireAnyways(action)

            if (!data.has(customItem.key) && !fireAnyways) {
                continue
            }

            if (player?.location?.let { item.isDisabled(it) } == true && !fireAnyways) {
                item.handleDisabled(player, event)
                return
            }
            if (!withContainer) {
                item.executeActions(action, player ?: getDummyPlayer() ?: return, event)
            } else {
                item.executeWithContainer(action, player ?: getDummyPlayer() ?: return, event, data)
            }
        }
    }

    fun fire(data: List<PersistentDataContainer>, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (itemData: PersistentDataContainer? in data.ifEmpty { listOf(null) }) {
            for (customItem: MutableMap.MutableEntry<NamespacedKey, CustomItem> in ItemManager.CUSTOM_ITEMS) {
                val fireAnyways = customItem.value.fireAnyways(action)

                if (!((itemData != null && itemData.has(customItem.key)) || fireAnyways)) {
                    continue
                }

                val item = customItem.value
                if (player?.location?.let { item.isDisabled(it) } == true && !fireAnyways) {
                    item.handleDisabled(player, event)
                    break
                }
                Executors.sync {
                    if (!withContainer) {
                        item.executeActions(action, player ?: getDummyPlayer() ?: return@sync, event)
                    } else {
                        item.executeWithContainer(action, player ?: getDummyPlayer() ?: return@sync, event, itemData!!)
                    }
                }
            }
        }
    }

    // TODO: @FireAnyways
    fun fire(key: String, action: Action, player: Player?, event: Any, withContainer: Boolean = false) {
        for (customItem in ItemManager.CUSTOM_ITEMS) {
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

    // Handle treefeller

    fun isTreeFeller(player: Player): Boolean {
        try {
            if (LumaItems.isWithmcMMO() && mcMMOAbilityAPI.treeFellerEnabled(player)) {
                return true
            }
        } catch (throwable: Throwable) {
            Logging.errorLog("Error checking mcMMO tree feller state for player ${player.name}", throwable)
        }
        return false
    }
}