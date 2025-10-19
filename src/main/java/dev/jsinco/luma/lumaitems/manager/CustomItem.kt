package dev.jsinco.luma.lumaitems.manager

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.events.items.ItemListener
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.Executors.sync
import dev.jsinco.luma.lumaitems.util.FireAnyways
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import io.papermc.paper.persistence.PersistentDataContainerView
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask

interface CustomItem {

    fun instance(): LumaItems {
        return LumaItems.getInstance()
    }
    fun random(): Random {
        return Random
    }

    fun <T> sync(block: () -> T): BukkitTask? {
        return Executors.sync { block() }
    }

    fun <T> async(block: () -> T): ScheduledTask {
        return Executors.async { block() }
    }

    /**
     * Called at startup to initialize and create each custom item
     * @return A pair of the item's nbt key and the itemstack
     */
    fun createItem(): Pair<String, ItemStack>


    /**
     * Called when a listener detects a custom item being used
     * @param type The type of ability to execute
     * @param player The player using the item
     * @param event The event that triggered the ability
     * @return A boolean for return info
     */
    fun executeActions(type: Action, player: Player, event: Any): Boolean

    fun executeWithContainer(type: Action, player: Player, event: Any, container: PersistentDataContainerView): Boolean {
        return executeActions(type, player, event)
    }


    fun asyncGlobalTask() {}

    fun isDisabled(inLocation: Location): Boolean {
        val disableAnnotation: Disable? = this::class.java.getAnnotation(Disable::class.java)
        disableAnnotation?.value?.forEach {
            if (it.isInWorld(inLocation)) {
                return true
            }
        }
        return false
    }

    fun isHardDisabled(): Boolean {
        val disableAnnotation: Disable = this::class.java.getAnnotation(Disable::class.java) ?: return false
        return disableAnnotation.hard
    }

    fun handleDisabled(player: Player, event: Any) {
        var persistNotif = false
        if (this.isHardDisabled() && event is Cancellable && event !is PlayerMoveEvent) {
            event.isCancelled = true
            persistNotif = true
        }
        ItemListener.notify(player, persistNotif)
    }

    fun fireAnyways(action: Action): Boolean {
        val annotation = this::class.java.getAnnotation(FireAnyways::class.java) ?: return false
        return annotation.value.contains(action)
    }
}