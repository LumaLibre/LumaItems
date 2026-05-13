package dev.lumas.lumaitems.events.item

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.equipmentContainers
import dev.lumas.lumaitems.util.extensions.equipmentSources
import dev.lumas.lumaitems.util.extensions.sync
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType


class PassiveListeners(val plugin: LumaItems) {

    companion object {
        const val DEFAULT_PASSIVE_LISTENER_TICKS: Long = 70
        const val ASYNC_PASSIVE_LISTENER_TICKS: Long = 30
        const val FAST_ASYNC_PASSIVE_LISTENER_TICKS: Long = 3
        const val ASYNC_GLOBAL_TASK_TICKS: Long = 20
    }

    // TODO: refactor
    private fun fire(dataList: List<PersistentDataContainer>, player: Player, action: Action) {
        for (data: PersistentDataContainer in dataList) {
            for (customItem in Registry.CUSTOM_ITEMS) {
                if (!data.has(customItem.key.asNameSpacedKey(), PersistentDataType.SHORT)) continue
                customItem.value.executeActions(action, player, 0)
            }
        }
    }

    // TODO: refactor
    private fun fire(action: Action) {
        for (customItem in Registry.CUSTOM_ITEMS.values()) {
            customItem.executeActions(action, ItemListener.getDummyPlayer() ?: return, 0)
        }
    }


    fun getPassiveListener(action: Action, period: Long, synchronize: Boolean): ScheduledTask {
        return Executors.asyncTimer(0, period) {
            for (player in Bukkit.getOnlinePlayers()) {
                if (synchronize) {
                    player.sync {
                        fire(player.equipmentContainers(), player, action)
                    }
                } else {
                    fire(player.equipmentContainers(), player, action)
                }
            }
        }
    }

    fun getGlobalTask(period: Long): ScheduledTask {
        return Executors.asyncTimer(0, period) {
            for (customItem in Registry.CUSTOM_ITEMS.values()) {
                customItem.asyncGlobalTask()
            }
        }
    }

    fun onPluginAction(action: Action) {
        for (player in Bukkit.getOnlinePlayers()) {
            fire(player.equipmentContainers(), player, action)
        }
    }

    fun onPluginActionGlobal(action: Action) {
        fire(action)
    }

}