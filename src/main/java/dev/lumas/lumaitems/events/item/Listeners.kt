package dev.lumas.lumaitems.events.item

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.annotations.AllSlots
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.block.BlockCacheManager
import dev.lumas.lumaitems.model.item.PdcSource
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.asSource
import dev.lumas.lumaitems.util.extensions.equipmentSources
import dev.lumas.lumaitems.util.extensions.handSources
import io.papermc.paper.event.entity.EntityAttemptSmashAttackEvent
import io.papermc.paper.event.entity.EntityCompostItemEvent
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent
import io.papermc.paper.event.entity.EntityMoveEvent
import io.papermc.paper.event.player.AsyncChatEvent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockShearEntityEvent
import org.bukkit.event.block.EntityBlockFormEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityExhaustionEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.entity.ItemMergeEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.EquipmentSlot

/**
 * Main listeners class for LumaItems
 * We use persistent data containers to store the custom item data and listen for it
 * Blocks cannot store persistent data, so we will have to store in a file (if needed for long term)
 * Or have our listeners fire every single executeAbilities() method every time we need to grab data from a block
 */
@Register(Autowire.LISTENER)
class Listeners : ItemListener() {

    @EventHandler
    fun onCrossbowLoad(event: EntityLoadCrossbowEvent) {
        val player = event.entity as? Player ?: return
        val source = event.crossbow.asSource() ?: return

        fire(source, Action.CROSSBOW_LOAD, player, event)
    }

    @EventHandler
    fun onPlayerBowShoot(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val source = event.bow.asSource() ?: return

        fire(source, Action.PLAYER_SHOOT_BOW, player, event)
    }

    @AllSlots
    @EventHandler
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val player: Player = event.entity.shooter as? Player ?: return
        fire(player.equipmentSources(), Action.PROJECTILE_LAUNCH, player, event)
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val player = event.entity.shooter as? Player ?: return
        // Projectile PDC — no source item
        fire(PdcSource.of(event.entity.persistentDataContainer), Action.PROJECTILE_LAND, player, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val sources = player.equipmentSources()
        val action: Action = when {
            event.action.isLeftClick -> Action.LEFT_CLICK
            event.action.isRightClick -> Action.RIGHT_CLICK
            else -> Action.GENERIC_INTERACT
        }

        fire(sources, action, player, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        fire(player.equipmentSources(), Action.SWAP_HAND, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity

        entity.killer?.let { player ->
            fire(player.equipmentSources(), Action.ENTITY_DEATH, player, event)
            return // We got a killer, we're done.
        }
        // No killer. Let's check the entity's persistent data container.
        fire(PdcSource.of(entity.persistentDataContainer), Action.ENTITY_DEATH, getDummyPlayer(), event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        fire(player.equipmentSources(), Action.PLAYER_DEATH, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val player: Player = when (val d = event.damager) {
            is Player -> d
            is Projectile -> d.shooter as? Player ?: return
            else -> return
        }

        val sources = player.equipmentSources().toMutableList().apply {
            (event.damager as? Projectile)?.persistentDataContainer
                ?.let { add(PdcSource.of(it)) }
        }

        fire(sources, Action.ENTITY_DAMAGE, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerDamagedByEntity(event: EntityDamageByEntityEvent) {
        val entity = event.entity
        val damager = event.damager

        if (entity is Player) {
            fire(entity.equipmentSources(), Action.PLAYER_DAMAGED_BY_ENTITY, entity, event)
        }

        if (damager is Player) {
            fire(PdcSource.of(entity.persistentDataContainer), Action.ENTITY_DAMAGED_BY_PLAYER, damager, event)
        }
    }

    @AllSlots
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity is Player) {
            fire(entity.equipmentSources(), Action.PLAYER_DAMAGED, entity, event)
        } else {
            fire(PdcSource.of(entity.persistentDataContainer), Action.ENTITY_DAMAGED_GENERIC, null, event)
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        val source = event.itemDrop.itemStack.asSource() ?: return

        fire(source, Action.DROP_ITEM, player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerBreakBlock(event: BlockBreakEvent) {
        val player = event.player
        if (this.isTreeFeller(player)) return

        player.inventory.itemInMainHand.asSource()?.let { source ->
            fire(source, Action.BREAK_BLOCK, player, event)
        }

        val playerCachedBlocks = BlockCacheManager.playerCachedBlocks[player.uniqueId] ?: return
        val l = event.block.location

        for (loc in playerCachedBlocks.locations) {
            if (loc == l) {
                fire(playerCachedBlocks.id, Action.CACHED_BLOCK_BREAK, player, event)
                break
            }
        }
    }

    @EventHandler
    fun onBlockDropItems(event: BlockDropItemEvent) {
        val player = event.player
        val source = player.inventory.itemInMainHand.asSource() ?: return

        fire(source, Action.BLOCK_DROP_ITEM, player, event)
    }


    @AllSlots
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerPlaceBlock(event: BlockPlaceEvent) {
        val player = event.player
        val source = event.itemInHand.asSource() ?: return

        fire(source, Action.PLACE_BLOCK, player, event)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockDamage(event: BlockDamageEvent) {
        val player = event.player
        val source = player.inventory.itemInMainHand.asSource() ?: return

        fire(source, Action.BLOCK_DAMAGE, player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerFish(event: PlayerFishEvent) {
        val player = event.player

        val mainHand = player.inventory.itemInMainHand
        val offHand = player.inventory.itemInOffHand

        if (!mainHand.hasItemMeta() && !offHand.hasItemMeta()) return

        // TODO: Look into why this listener is written this way instead of using Util.getHandSources()
        val mainSource = mainHand.asSource()
        val offSource = offHand.asSource()
        for (entry in Registry.CUSTOM_ITEMS) {
            val key = entry.key.asNameSpacedKey()
            when {
                mainSource?.data?.has(key) == true -> {
                    entry.value.executeActions(Action.FISH, player, event)
                    break
                }
                offSource?.data?.has(key) == true -> {
                    entry.value.executeActions(Action.FISH, player, event)
                    break
                }
            }
        }
    }

    @EventHandler
    fun onPlayerElytraBoost(event: PlayerElytraBoostEvent) {
        val player = event.player
        val elytra = player.inventory.chestplate
        val source = elytra.asSource() ?: return

        fire(source, Action.ELYTRA_BOOST, player, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerCrouch(event: PlayerToggleSneakEvent) {
        val player = event.player
        fire(player.equipmentSources(), Action.PLAYER_CROUCH, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        fire(player.equipmentSources(), Action.ASYNC_CHAT, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        fire(player.equipmentSources(), Action.COMMAND_PREPROCESS, player, event)
    }


    @AllSlots
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasChangedPosition()) return
        fire(event.player.equipmentSources(), Action.MOVE, event.player, event, optimize = true)
    }

    @AllSlots
    //@EventHandler
    fun onPlayerInput(event: PlayerInputEvent) {
        fire(event.player.equipmentSources(), Action.INPUT, event.player, event, optimize = true)
    }

    //@EventHandler
    fun onEntityMoveEvent(event: EntityMoveEvent) {
        if (!event.hasChangedPosition() || !event.entity.hasAI()) return
        val action = Action.ENTITY_MOVE
        if (!action.canFireRightNow()) return

        val container = event.entity.persistentDataContainer
        if (container.isEmpty) return

        fire(PdcSource.of(container), action, null, event, optimize = true)
    }

    @EventHandler
    fun onPlayerJump(event: PlayerJumpEvent) {
        fire(event.player.equipmentSources(), Action.JUMP, event.player, event, optimize = true)
    }

    @EventHandler
    fun onEntityFormBlock(event: EntityBlockFormEvent) {
        val container = event.entity.persistentDataContainer
        if (container.isEmpty) return

        fire(PdcSource.of(container), Action.ENTITY_FORM_BLOCK, event.entity as? Player, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerConsumeItem(event: PlayerItemConsumeEvent) {
        fire(event.player.equipmentSources(), Action.CONSUME_ITEM, event.player, event)
    }

    @AllSlots
    @EventHandler
    fun onEntityPotionEffect(event: EntityPotionEffectEvent) {
        val player = event.entity as? Player ?: return
        fire(player.equipmentSources(), Action.POTION_EFFECT, player, event, optimize = true)
    }

    @AllSlots
    @EventHandler
    fun onEntityTargetLivingEntity(event: EntityTargetLivingEntityEvent) {
        val target = event.target as? Player ?: return
        fire(PdcSource.of(event.entity.persistentDataContainer), Action.ENTITY_TARGET_PLAYER, target, event)
        fire(target.equipmentSources(), Action.ENTITY_TARGET_PLAYER, target, event)
    }


    @EventHandler
    fun onPlayerArmorSwap(event: PlayerArmorChangeEvent) {
        val sources = listOfNotNull(
            event.oldItem.asSource(),
            event.newItem.asSource()
        )

        fire(sources, Action.ARMOR_CHANGE, event.player, event)
    }

    @EventHandler
    fun onEntityTeleport(event: EntityTeleportEvent) {
        val container = event.entity.persistentDataContainer
        if (container.isEmpty) return

        fire(PdcSource.of(container), Action.ENTITY_TELEPORT, null, event)
    }

    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        fire(event.player.handSources(), Action.PLAYER_INTERACT_AT_ENTITY, event.player, event)
    }

    //@EventHandler unused
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        fire(event.player.handSources(), Action.PLAYER_INTERACT_ENTITY, event.player, event)
    }

    @EventHandler
    fun onShearEntity(event: PlayerShearEntityEvent) {
        val player = event.player
        val source = player.inventory.itemInMainHand.asSource() ?: return

        fire(source, Action.SHEAR_ENTITY, player, event)
    }

    @EventHandler
    fun onBlockShearEntity(event: BlockShearEntityEvent) {
        val source = event.tool.asSource() ?: return

        fire(source, Action.BLOCK_SHEAR_ENTITY, null, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        fire(event.player.equipmentSources(), Action.PLAYER_TELEPORT, event.player, event, optimize = true)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        fire(event.player.equipmentSources(), Action.PLAYER_QUIT, event.player, event, optimize = true)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        fire(event.player.equipmentSources(), Action.PLAYER_JOIN, event.player, event, optimize = true)
    }

    @AllSlots
    @EventHandler
    fun onPlayerPickupExp(event: PlayerPickupExperienceEvent) {
        fire(event.player.equipmentSources(), Action.PLAYER_PICKUP_EXP, event.player, event)
    }

    // ONLY FIRES IF PERSISTENT DATA IS IN THE ITEMSTACK!!!!
    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        val source = event.item.itemStack.asSource() ?: return
        fire(source, Action.ENTITY_PICKUP_ITEM, null, event)
    }

    // Called when a hopper or hopper minecart picks up a dropped item.
    @EventHandler
    fun onHopperPickupEvent(event: InventoryPickupItemEvent) {
        val source = event.item.itemStack.asSource() ?: return
        fire(source, Action.HOPPER_PICKUP_ITEM, null, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val cursorSlotType = event.slotType
        if ((cursorSlotType == InventoryType.SlotType.ARMOR || cursorSlotType == InventoryType.SlotType.QUICKBAR) && isHardDisabledAt(event.cursor, player.location)) {
            event.isCancelled = true
            notify(player, true)
            return
        }
        val action = event.action
        if ((event.isShiftClick || action == InventoryAction.HOTBAR_SWAP) &&
            isHardDisabledAt(event.currentItem, player.location)) {
            event.isCancelled = true
            notify(player, true)
            return
        }
        val sources = mutableListOf<PdcSource>()
        event.currentItem.asSource()?.let(sources::add)
        event.cursor.asSource()?.let(sources::add)
        if (sources.isEmpty()) return
        fire(sources, Action.INVENTORY_CLICK, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInventoryOpen(event: InventoryOpenEvent) {
        val player = event.player as? Player ?: return
        fire(player.equipmentSources(), Action.INVENTORY_OPEN, player, event)
    }

    @AllSlots
    //@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true) unused
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as? Player ?: return
        fire(player.equipmentSources(), Action.INVENTORY_CLOSE, player, event)
    }

    @EventHandler
    fun onPlayerFillBucket(event: PlayerBucketFillEvent) {
        val player = event.player
        val source = event.itemStack.asSource() ?: return

        fire(source, Action.FILL_BUCKET, player, event)
    }

    @EventHandler
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        val player = event.player
        val item = when (event.hand) {
            EquipmentSlot.HAND -> player.inventory.itemInMainHand
            EquipmentSlot.OFF_HAND -> player.inventory.itemInOffHand
            else -> return
        }
        val source = item.asSource() ?: return
        fire(source, Action.EMPTY_BUCKET, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerPickupItem(event: PlayerAttemptPickupItemEvent) {
        val player = event.player
        val action = Action.PICKUP_ITEM
        if (!action.canFireRightNow()) return

        fire(player.equipmentSources(), action, player, event, true)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // if you modify this event to include previous slot, items that use this need a conditional added to their logic
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
//        val sources = listOfNotNull(
//            player.inventory.getItem(event.newSlot).asSource(),
//            player.inventory.getItem(event.previousSlot).asSource()
//        ).ifEmpty { return }
        if (isHardDisabledAt(player.inventory.getItem(event.newSlot), player.location)) {
            event.isCancelled = true
            notify(player, true)
            return
        }
        fire(player.equipmentSources(), Action.ITEM_HELD, player, event)
    }

    @EventHandler
    fun onPlayerItemDamage(event: PlayerItemDamageEvent) {
        val player = event.player
        val source = event.item.asSource() ?: return

        fire(source, Action.ITEM_DAMAGE, player, event)
    }

    @EventHandler
    fun onEntityKnockbackByEntity(event: EntityKnockbackByEntityEvent) {
        val player = event.hitBy as? Player ?: return
        val source = player.inventory.itemInMainHand.asSource() ?: return

        fire(source, Action.PLAYER_KNOCKBACK_ENTITY, player, event)
    }

    @EventHandler
    fun onItemMerge(event: ItemMergeEvent) {
        val player = event.target.thrower?.let { Bukkit.getPlayer(it) } ?: return
        // Item entity PDC, not the ItemStack's — no source item
        fire(PdcSource.of(event.target.persistentDataContainer), Action.ITEM_MERGE, player, event)
    }

    @EventHandler
    fun onSmashAttack(event: EntityAttemptSmashAttackEvent) {
        val player = event.entity as? Player ?: return
        val source = player.inventory.itemInMainHand.asSource() ?: return

        fire(source, Action.MACE_SMASH_ATTACK, player, event)
    }

    @AllSlots
    //@EventHandler Unused
    fun onPlayerResurrect(event: EntityResurrectEvent) {
        val player = event.entity as? Player ?: return
        fire(player.equipmentSources(), Action.PLAYER_RESURRECT, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityExhaustion(event: EntityExhaustionEvent) {
        val player = event.entity as? Player ?: return
        fire(player.equipmentSources(), Action.ENTITY_EXHAUSTION, player, event, optimize = true)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val player = event.view.player as? Player ?: return
        val sources = event.inventory.matrix
            .mapNotNull { it.asSource() }
            .ifEmpty { return }
        fire(sources, Action.PREPARE_CRAFT, player, event)
    }

    @EventHandler
    fun onEntityCompostItem(event: EntityCompostItemEvent) {
        val source = event.item.asSource() ?: return
        fire(source, Action.ENTITY_COMPOST_ITEM, event.entity as? Player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCraftItem(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        val sources = event.inventory.matrix
            .mapNotNull { it.asSource() }
            .ifEmpty { return }
        fire(sources, Action.CRAFT_ITEM, player, event)
    }
}