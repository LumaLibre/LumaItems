package dev.jsinco.luma.lumaitems.manager

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

interface CustomItem {

    fun instance(): LumaItems {
        return LumaItems.getInstance()
    }
    fun random(): Random {
        return Random
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

    fun isDisabled(inLocation: Location): Boolean {
        val disableAnnotation: Disable? = this::class.java.getAnnotation(Disable::class.java)
        disableAnnotation?.value?.forEach {
            if (it.isInWorld(inLocation)) {
                return true
            }
        }
        return false
    }
}