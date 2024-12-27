package dev.jsinco.luma.items.misc

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.events.PacketContainer
import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class LoversAlluringItem : CustomItem {
    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fb4d48&lL&#fb5555&lo&#fc5d63&lv&#fc6570&le&#fc6e7e&lr&#fc768b&l'&#fd7e99&ls &#fd86a6&lA&#fa88b2&ll&#f88bbe&ll&#f58dca&lu&#f290d7&lr&#ef92e3&li&#ed95ef&ln&#ea97fb&lg",
            mutableListOf("&#FD86A6Worker", "&#FD86A6Coupled"),
            mutableListOf("When fishing with this rod, bites", "will automatically be reeled in", "", "Fish caught with this rod have", "a small chance to be doubled"),
            Material.FISHING_ROD,
            mutableListOf("lovers-alluring"),
            mutableMapOf(Enchantment.LURE to 5, Enchantment.LUCK_OF_THE_SEA to 5, Enchantment.UNBREAKING to 9, Enchantment.MENDING to 1),
        )
        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"
        return Pair("lovers-alluring", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.FISH -> {
                event as PlayerFishEvent
                when (event.state) {
                    PlayerFishEvent.State.BITE -> {
                        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
                            LumaItems.getProtocolManager()?.receiveClientPacket(player, PacketContainer(PacketType.Play.Client.USE_ITEM))
                        }, 1L)
                    }
                    PlayerFishEvent.State.CAUGHT_FISH -> {
                        if (Random.nextInt(100) > 10) return false
                        val item = event.caught as Item
                        if (item.itemStack.maxStackSize > 1) item.itemStack.amount = 2
                    }
                    else -> return false
                }
            }
            else -> return false
        }
        return true
    }
}