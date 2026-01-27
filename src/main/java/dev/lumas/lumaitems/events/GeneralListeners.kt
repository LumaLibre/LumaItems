package dev.lumas.lumaitems.events

import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.enums.EntityArmor
import dev.lumas.lumaitems.enums.GenericToolType
import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.guis.AbstractGui
import dev.lumas.lumaitems.guis.DisassemblerGui
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.registry.NamespacedIdentifier
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.relics.RelicCreator
import dev.lumas.lumaitems.relics.RelicDisassembler
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Util
import kotlin.random.Random
import org.bukkit.Bukkit
import org.bukkit.Material
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

// TODO: These listeners are a mess
@AutoRegister(RegisterType.LISTENER)
class GeneralListeners : Listener {

    companion object {
        private val LUMA_ITEM_KEY = Util.namespacedKey("lumaitem")
        private val BOSSES: List<EntityType> = listOf(
            EntityType.ENDER_DRAGON,
            EntityType.WITHER,
            EntityType.ELDER_GUARDIAN,
            EntityType.WARDEN
        )
    }

    @EventHandler
    fun onEntitySpawn(event: EntitySpawnEvent) {
        val livingEntity = event.entity as? LivingEntity ?: return
        val isBoss = BOSSES.contains(livingEntity.type)

        if (Random.nextInt(101) > 8 || livingEntity !is Enemy) return // 8% chance to spawn a relic

        Executors.asyncDelayed(1) {
            if (livingEntity.hasMetadata("NO_RELIC")) return@asyncDelayed

            val rarity: Rarity = if (isBoss) Rarity.bossRarities[0] else Rarity.genericRarities.random()
            val material = rarity.materials.random()

            val relic = RelicCreator(
                rarity.algorithmWeight,
                -1,
                rarity,
                material
            ).getRelicItem()

            Executors.sync {
                if (GenericToolType.getGenericToolType(relic.type) == GenericToolType.ARMOR) {
                    val entityArmor = EntityArmor.getEquipmentSlotFromType(relic.type)
                    entityArmor?.setEntityArmorSlot(livingEntity, relic)
                } else if (livingEntity.type == EntityType.WITHER_SKELETON && relic.type == Material.BOW) {
                    livingEntity.equipment?.setHelmet(relic)
                } else {
                    livingEntity.equipment?.setItemInOffHand(relic)
                }
            }

        }

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


        if (!RelicDisassembler.DISASSEMBLER_BLOCKS.contains(event.clickedBlock ?: return)) return

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
            player.setItemOnCursor(hatItem);
            player.inventory.helmet = cursorItem;player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f)
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

        if (meta.persistentDataContainer.has(LUMA_ITEM_KEY, PersistentDataType.SHORT)) {
            cancelEvent = true
        } else {
            for (key in Registry.CUSTOM_ITEM_REGISTRY.keySet(NamespacedIdentifier::class)) {
                if (meta.persistentDataContainer.has(key.key(), PersistentDataType.SHORT)) {
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