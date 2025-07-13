package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.NamespacedKey
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import dev.jsinco.luma.lumaitems.enums.Action
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.persistence.PersistentDataType
import java.util.*

class WindforgedRocket : CustomItemFunctions() {

    companion object {
        private const val CUSTOM_ITEM_KEY = "windforged-rocket"
    }

    private val plugin = JavaPlugin.getProvidingPlugin(this::class.java)
    private val key = NamespacedKey(plugin, CUSTOM_ITEM_KEY)
    private val uuid = NamespacedKey(plugin, "uuid")

    private fun createWindforgedRocket(): ItemStack {
        val windforgedRocket = ItemFactory.builder()
            .name("<b><gradient:#E90000:#E90000>Wind</gradient><gradient:#E90000:#FFFFFF>forged Roc</gradient><gradient:#FFFFFF:#FFFFFF>ket</gradient></b>")
            .customEnchants("<#D42424>Boundless Boost")
            .lore(
                "Forged from the breath of Zephyr,",
                "eternal wind of the western skies."
            )
            .material(Material.FIREWORK_ROCKET)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .tier(Tier.SUMMER_2025)
            .persistentData(CUSTOM_ITEM_KEY)
            .build()
            .createItem()

        val meta = windforgedRocket.itemMeta as FireworkMeta
        meta.power = 3
        meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
        meta.persistentDataContainer.set(uuid, PersistentDataType.STRING, UUID.randomUUID().toString()) // Make it unstackable?
        windforgedRocket.itemMeta = meta

        return windforgedRocket
    }

    override fun createItem(): Pair<String, ItemStack> {
        return Pair(CUSTOM_ITEM_KEY, createWindforgedRocket())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when(type) {
            Action.ELYTRA_BOOST -> {
                val elytraBoostEvent = event as? PlayerElytraBoostEvent ?: return false
                return handleActions(player, elytraBoostEvent.hand, elytraBoostEvent.itemStack, )
            }
            Action.RIGHT_CLICK -> {
                val rightClickEvent = event as? PlayerInteractEvent ?: return false
                return handleActions(player, rightClickEvent.hand, rightClickEvent.item)
            }
            else -> return false
        }
    }

    // Helper function(s)
    private fun handleActions(player: Player, slot: EquipmentSlot?, item: ItemStack?): Boolean {
        if(slot == null || item == null || !isWindforgedRocket(item)) return false
        scheduleRestoreIfConsumed(player, slot)
        return true
    }

    private fun isWindforgedRocket(item: ItemStack?): Boolean {
        if(item == null || item.type != Material.FIREWORK_ROCKET) return false
        val meta = item.itemMeta ?: return false
        return meta.persistentDataContainer.has(key, PersistentDataType.BYTE)
    }

    private fun scheduleRestoreIfConsumed(player: Player, slot: EquipmentSlot) {
        plugin.server.scheduler.runTaskLater(plugin, Runnable {
            if(player.inventory.getItem(slot).isEmpty)
                player.inventory.setItem(slot, createWindforgedRocket())
        }, 1L)
    }

}