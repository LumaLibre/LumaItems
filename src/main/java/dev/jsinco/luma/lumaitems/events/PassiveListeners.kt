package dev.jsinco.luma.lumaitems.events

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.ItemManager
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable


class PassiveListeners(val plugin: LumaItems) {

    companion object {
        const val DEFAULT_PASSIVE_LISTENER_TICKS: Long = 70
        const val ASYNC_PASSIVE_LISTENER_TICKS: Long = 30
    }

    private fun fire(dataList: List<PersistentDataContainer>, player: Player, action: Action) {
        for (data: PersistentDataContainer in dataList) {
            for (customItem in ItemManager.customItems) {
                if (!data.has(customItem.key, PersistentDataType.SHORT)) continue
                customItem.value.executeActions(action, player, 0)
            }
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

    fun onPluginAction(action: Action) {
        for (player in Bukkit.getOnlinePlayers()) {
            fire(Util.getAllEquipmentNBT(player), player, action)
        }
    }

}