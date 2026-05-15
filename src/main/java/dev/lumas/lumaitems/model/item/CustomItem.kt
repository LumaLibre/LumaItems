package dev.lumas.lumaitems.model.item

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.events.item.ItemListener
import dev.lumas.lumaitems.registry.NamespacedIdentifier
import dev.lumas.lumaitems.registry.RegistryItem
import dev.lumas.lumaitems.util.extensions.Executors
import io.papermc.paper.persistence.PersistentDataContainerView
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack

interface CustomItem : RegistryItem {

    fun instance(): LumaItems {
        return LumaItems.getInstance()
    }
    fun random(): Random {
        return Random.Default
    }

    val random: Random
        get() = Random.Default

    val instance: LumaItems
        get() = LumaItems.getInstance()

    fun <T> async(block: () -> T): ScheduledTask {
        return Executors.async { block() }
    }

    fun <T> global(block: () -> T): ScheduledTask {
        return Executors.global { block() }
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
        val disable = this::class.java.getAnnotation(Disable::class.java) ?: return false
        return disable.value.any { if (disable.invert) !it.isInWorld(inLocation) else it.isInWorld(inLocation) }
    }

    fun isHardDisabled(): Boolean {
        val disableAnnotation: Disable = this::class.java.getAnnotation(Disable::class.java) ?: return false
        return disableAnnotation.hard
    }

    fun handleDisabled(player: Player, event: Any) {
        var persistNotif = false
        if (this.isHardDisabled() && event is Cancellable && event !is PlayerMoveEvent && event !is PlayerJumpEvent && event !is PlayerItemHeldEvent && event !is InventoryClickEvent) {
            event.isCancelled = true
            persistNotif = true
        }
        ItemListener.notify(player, persistNotif)
    }

    fun fireAnyways(action: Action): Boolean {
        return false
    }

    fun tabCompleteName(): String? {
        return null
    }

    override fun identifier(): NamespacedIdentifier {
        return NamespacedIdentifier.lumaitems(this.createItem().first)
    }
}