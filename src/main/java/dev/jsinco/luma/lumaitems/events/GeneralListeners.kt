package dev.jsinco.luma.lumaitems.events

import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.guis.AbstractGui
import dev.jsinco.luma.lumaitems.guis.DisassemblerGui
import dev.jsinco.luma.lumaitems.manager.FileManager
import dev.jsinco.luma.lumaitems.manager.ItemManager
import dev.jsinco.luma.lumaitems.enums.Rarity
import dev.jsinco.luma.lumaitems.relics.RelicCreator
import dev.jsinco.luma.lumaitems.relics.RelicDisassembler
import dev.jsinco.luma.lumaitems.enums.EntityArmor
import dev.jsinco.luma.lumaitems.enums.ToolType
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.entity.Enemy
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.random.Random

// TODO: These listeners are a mess
@AutoRegister(RegisterType.LISTENER)
class GeneralListeners : Listener {

    val plugin: LumaItems = LumaItems.getInstance()

    companion object {
        val relicFile = FileManager("relics.yml").generateYamlFile()
        private val bosses: List<EntityType> = listOf(
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.ELDER_GUARDIAN,
            EntityType.WARDEN
        )
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val livingEntity = event.entity as? LivingEntity ?: return
        val isBoss = bosses.contains(livingEntity.type)

        if (Random.nextInt(100) > 8 || livingEntity !is Enemy) return // 8% chance to spawn a relic

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            if (livingEntity.hasMetadata("NO_RELIC")) return@Runnable

            val rarity: Rarity = if (isBoss) Rarity.bossRarities[0] else Rarity.genericRarities.random()
            val material: Material =
                Material.valueOf(relicFile.getStringList("relic-materials.${rarity.name.lowercase()}").random())

            val relic = RelicCreator(
                rarity.algorithmWeight,
                -1,
                rarity,
                material
            ).getRelicItem()

            Bukkit.getScheduler().runTask(plugin, Runnable {
                if (ToolType.getToolType(relic.type) == ToolType.ARMOR) {
                    val entityArmor = EntityArmor.getEquipmentSlotFromType(relic.type)
                    entityArmor?.setEntityArmorSlot(livingEntity, relic)
                } else {
                    livingEntity.equipment?.setItemInOffHand(relic)
                }
            })
        }, 1L)
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        if (item.hasItemMeta() && Util.hasPersistentKey(item.itemMeta, ItemFactory.AUTO_HAT_KEY)) { // TODO: Organize
            if ((event.action != Action.RIGHT_CLICK_AIR && event.action != Action.RIGHT_CLICK_BLOCK)) return

            val currentHelm = player.equipment?.helmet
            if (currentHelm == null || currentHelm.isEmpty) {
                player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_DIAMOND, 1f, 1f)
                player.equipment?.helmet = item.asOne()
                item.amount -= 1
            }

            event.isCancelled = true
            return
        }


        if (!RelicDisassembler.disassemblerBlocks.contains(event.clickedBlock ?: return)) return

        event.isCancelled = true


        if (event.action == Action.LEFT_CLICK_BLOCK && player.hasPermission("lumaitems.disassemblergui")) {
            val gui = DisassemblerGui()
            player.openInventory(gui.getInventory())
            return
        }

        val command = RelicDisassembler.getCommandToExecute(item, event.action, player) ?: return

        item.amount -= 1
        player.playSound(player.location, Sound.ENTITY_SQUID_SQUIRT, 1f, 0.9f)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command)
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.inventory.getHolder(false) is AbstractGui) {
            (event.inventory.holder as AbstractGui).onInventoryClick(event)
            return
        }

        val player = event.whoClicked as Player
        val item = player.itemOnCursor
        if (item.hasItemMeta() && Util.hasPersistentKey(item.itemMeta, ItemFactory.AUTO_HAT_KEY)) { // TODO: Organize
            if (event.inventory.type != InventoryType.CRAFTING || event.slotType != InventoryType.SlotType.ARMOR) return
            val cursorItem = player.itemOnCursor.clone()
            val hatItem = player.inventory.helmet

            if (cursorItem.type == Material.AIR || cursorItem == player.inventory.helmet) return
            event.isCancelled = true
            player.setItemOnCursor(hatItem);player.inventory.helmet = cursorItem;player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f)
        }
    }


    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory.getHolder(false) !is AbstractGui) return
        (event.inventory.holder as AbstractGui).onInventoryClose(event)
    }


    @EventHandler
    fun onAnvilPrepare(event: PrepareAnvilEvent) {
        if (event.result == null || !event.result!!.hasItemMeta()) {
            return
        }

        val meta = event.result!!.itemMeta
        var cancelEvent = false

        if (meta.persistentDataContainer.has(NamespacedKey(plugin, "lumaitem"), PersistentDataType.SHORT)) {
            cancelEvent = true
        } else {
            for (key in ItemManager.customItems.keys) {
                if (meta.persistentDataContainer.has(key, PersistentDataType.SHORT)) {
                    cancelEvent = true
                    break
                }
            }
        }


        if (cancelEvent && event.inventory.secondItem != null) {
            event.result = null
        }
    }
}