package dev.lumas.lumaitems.events.items

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.ItemManager
import dev.lumas.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable


class PassiveListeners(val plugin: LumaItems) {

    companion object {
        const val DEFAULT_PASSIVE_LISTENER_TICKS: Long = 70
        const val ASYNC_PASSIVE_LISTENER_TICKS: Long = 30
        const val FAST_ASYNC_PASSIVE_LISTENER_TICKS: Long = 3
        const val ASYNC_GLOBAL_TASK_TICKS: Long = 20
    }

    private fun fire(dataList: List<PersistentDataContainer>, player: Player, action: Action) {
        for (data: PersistentDataContainer in dataList) {
            for (customItem in ItemManager.CUSTOM_ITEMS) {
                if (!data.has(customItem.key, PersistentDataType.SHORT)) continue
                customItem.value.executeActions(action, player, 0)
            }
        }
    }

    private fun fire(action: Action) {
        for (customItem in ItemManager.CUSTOM_ITEMS) {
            customItem.value.executeActions(action, ItemListener.getDummyPlayer() ?: return, 0)
        }
    }


    fun getPassiveListener(action: Action): BukkitRunnable {
        return object: BukkitRunnable() {
            override fun run() {
                for (player in Bukkit.getOnlinePlayers()) {
                    fire(Util.getAllEquipmentNBT(player), player, action)
                }
            }
        }
    }

    fun getGlobalTask(): BukkitRunnable {
        return object: BukkitRunnable() {
            override fun run() {
                for (customItem in ItemManager.CUSTOM_ITEMS) {
                    customItem.value.asyncGlobalTask()
                }
            }
        }
    }

    fun onPluginAction(action: Action) {
        for (player in Bukkit.getOnlinePlayers()) {
            fire(Util.getAllEquipmentNBT(player), player, action)
        }
    }

    fun onPluginActionGlobal(action: Action) {
        fire(action)
    }

}