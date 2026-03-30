package dev.lumas.lumaitems.events.items

import com.destroystokyo.paper.event.entity.EntityKnockbackByEntityEvent
import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.annotations.AllSlots
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.equipmentContainers
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
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
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
import org.bukkit.persistence.PersistentDataContainer

/**
 * Main listeners class for LumaItems
 * We use persistent data containers to store the custom item data and listen for it
 * Blocks cannot store persistent data, so we will have to store in a file (if needed for long term)
 * Or have our listeners fire every single executeAbilities() method every time we need to grab data from a block
 */
@AutoRegister(RegisterType.LISTENER)
class Listeners : ItemListener() {

    @EventHandler
    fun onCrossbowLoad(event: EntityLoadCrossbowEvent) {
        val player = event.entity as? Player ?: return
        val data: PersistentDataContainer = event.crossbow.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.CROSSBOW_LOAD, player, event)
    }

    @EventHandler
    fun onPlayerBowShoot(event: EntityShootBowEvent) {
        val player = event.entity as? Player ?: return
        val data: PersistentDataContainer = event.bow?.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.PLAYER_SHOOT_BOW, player, event)
    }

    @AllSlots
    @EventHandler
    fun onProjectileLaunch(event: ProjectileLaunchEvent) {
        val player: Player = event.entity.shooter as? Player ?: return
        fire(Util.getAllEquipmentNBT(player), Action.PROJECTILE_LAUNCH, player, event)
    }

    @EventHandler
    fun onProjectileHit(event: ProjectileHitEvent) {
        val player = event.entity.shooter as? Player ?: return

        val data = event.entity.persistentDataContainer
        fire(data, Action.PROJECTILE_LAND, player, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val player = event.player
        val dataContainers: List<PersistentDataContainer> = Util.getAllEquipmentNBT(player)
        val action: Action = if (event.action.isLeftClick) Action.LEFT_CLICK else if (event.action.isRightClick) Action.RIGHT_CLICK else Action.GENERIC_INTERACT

        fire(dataContainers, action, player, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerSwapHandItems(event: PlayerSwapHandItemsEvent) {
        val player = event.player
        //val item = event.offHandItem
        //item.itemMeta?.persistentDataContainer
        val data = Util.getAllEquipmentNBT(player) //?: return

        fire(data, Action.SWAP_HAND, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity

        entity.killer?.let { player ->
            val data = Util.getAllEquipmentNBT(player)
            fire(data, Action.ENTITY_DEATH, player, event)
            return // We got a killer, we're done.
        }
        // No killer. Let's check the entity's persistent data container.
        val data: PersistentDataContainer = entity.persistentDataContainer
        fire(data, Action.ENTITY_DEATH, getDummyPlayer(), event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val data = Util.getAllEquipmentNBT(player)

        fire(data, Action.PLAYER_DEATH, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityDamageByEntity(event: EntityDamageByEntityEvent) {
        val player: Player = when (event.damager) {
            is Player -> event.damager as Player
            is Projectile -> (event.damager as? Projectile)?.shooter as? Player ?: return
            else -> return
        }

        val data = Util.getAllEquipmentNBT(player).toMutableList().apply {
            (event.damager as? Projectile)?.persistentDataContainer?.let { add(it) }
        }

        fire(data, Action.ENTITY_DAMAGE, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onPlayerDamagedByEntity(event: EntityDamageByEntityEvent) {
        val player: Player = event.entity as? Player ?: return

        fire(Util.getAllEquipmentNBT(player), Action.PLAYER_DAMAGED_BY_ENTITY, player, event)
    }

    @AllSlots
    @EventHandler
    fun onEntityDamage(event: EntityDamageEvent) {
        val entity = event.entity
        val data: List<PersistentDataContainer>? = if (entity is Player) Util.getAllEquipmentNBT(entity) else null


        if (data != null) {
            fire(data, Action.PLAYER_DAMAGED, entity as? Player ?: return, event)
        } else {
            fire(entity.persistentDataContainer, Action.ENTITY_DAMAGED_GENERIC, null, event)
        }
    }

    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        val player = event.player
        val data: PersistentDataContainer = event.itemDrop.itemStack.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.DROP_ITEM, player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerBreakBlock(event: BlockBreakEvent) {
        val player = event.player
        if (this.isTreeFeller(player)) return
        val data: PersistentDataContainer? = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer

        if (data != null) {
            fire(data, Action.BREAK_BLOCK, player, event)
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
        val data = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer

        if (data != null) {
            fire(data, Action.BLOCK_DROP_ITEM, player, event)
        }
    }


    @AllSlots
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerPlaceBlock(event: BlockPlaceEvent) {
        val player = event.player
        val data = event.itemInHand.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.PLACE_BLOCK, player, event)
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockDamage(event: BlockDamageEvent) {
        val player = event.player
        val data: PersistentDataContainer = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.BLOCK_DAMAGE, player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPlayerFish(event: PlayerFishEvent) {
        val player = event.player

        val item = player.inventory.itemInMainHand
        val offHandItem = player.inventory.itemInOffHand

        if (!item.hasItemMeta() && !offHandItem.hasItemMeta()) return

        // TODO: Look into why this listener is written this way instead of using Util.getHandNBT()
        val data: PersistentDataContainer? = item.itemMeta?.persistentDataContainer
        val offHandData: PersistentDataContainer? = offHandItem.itemMeta?.persistentDataContainer
        for (entry in Registry.CUSTOM_ITEMS) {
            val key = entry.key.asNameSpacedKey()
            if (data?.has(key) == true) {
                entry.value.executeActions(Action.FISH, player, event)
                break
            } else if (offHandData?.has(key) == true) {
                entry.value.executeActions(Action.FISH, player, event)
                break
            }
        }
    }

    @EventHandler
    fun onPlayerElytraBoost(event: PlayerElytraBoostEvent) {
        val player = event.player

        val elytra = player.inventory.chestplate ?: return
        val data: PersistentDataContainer = elytra.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.ELYTRA_BOOST, player, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerCrouch(event: PlayerToggleSneakEvent) {
        val player = event.player
        fire(Util.getAllEquipmentNBT(player), Action.PLAYER_CROUCH, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onPlayerChat(event: AsyncChatEvent) {
        val player = event.player
        val data = player.equipmentContainers()
        fire(data, Action.ASYNC_CHAT, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onPlayerCommandPreprocess(event: PlayerCommandPreprocessEvent) {
        val player = event.player
        fire(player.equipmentContainers(), Action.COMMAND_PREPROCESS, player, event)
    }


    @AllSlots
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        if (!event.hasChangedPosition()) return
        fire(Util.getAllEquipmentNBT(event.player), Action.MOVE, event.player, event)
    }

    @AllSlots
    //@EventHandler
    fun onPlayerInput(event: PlayerInputEvent) {
        fire(Util.getAllEquipmentNBT(event.player), Action.INPUT, event.player, event)
    }

    //@EventHandler
    fun onEntityMoveEvent(event: EntityMoveEvent) {
        if (!event.hasChangedPosition() || !event.entity.hasAI()) return
        val action = Action.ENTITY_MOVE
        if (!action.canFireRightNow()) return

        val container: PersistentDataContainer = event.entity.persistentDataContainer
        if (container.isEmpty) return

        fire(container, action, null, event)
    }

    @EventHandler
    fun onPlayerJump(event: PlayerJumpEvent) {
        val data = Util.getAllEquipmentNBT(event.player)
        fire(data, Action.JUMP, event.player, event)
    }

    @EventHandler
    fun onEntityFormBlock(event: EntityBlockFormEvent) {
        val entity = event.entity

        val data: PersistentDataContainer = entity.persistentDataContainer
        if (data.isEmpty) return

        fire(data, Action.ENTITY_FORM_BLOCK, null, event)
    }

    @EventHandler
    fun onPlayerConsumeItem(event: PlayerItemConsumeEvent) {
        val data: PersistentDataContainer = event.item.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.CONSUME_ITEM, event.player, event)
    }

    @AllSlots
    @EventHandler
    fun onEntityPotionEffect(event: EntityPotionEffectEvent) {
        val player = event.entity as? Player ?: return
        fire(Util.getAllEquipmentNBT(player), Action.POTION_EFFECT, player, event)
    }

    @EventHandler
    fun onEntityTargetLivingEntity(event: EntityTargetLivingEntityEvent) {
        val target = event.target as? Player ?: return
        val data = event.entity.persistentDataContainer

        fire(data, Action.ENTITY_TARGET_PLAYER, target, event)
    }


    @EventHandler
    fun onPlayerArmorSwap(event: PlayerArmorChangeEvent) {
        val data = listOf(event.oldItem, event.newItem).mapNotNull { it.itemMeta?.persistentDataContainer }

        fire(data, Action.ARMOR_CHANGE, event.player, event)
    }

    @EventHandler
    fun onEntityTeleport(event: EntityTeleportEvent) {
        val container = event.entity.persistentDataContainer
        if (container.isEmpty) return

        fire(container, Action.ENTITY_TELEPORT, null, event)
    }

    @EventHandler
    fun onPlayerInteractAtEntity(event: PlayerInteractAtEntityEvent) {
        fire(Util.getHandNBT(event.player), Action.PLAYER_INTERACT_AT_ENTITY, event.player, event)
    }

    //@EventHandler unused
    fun onPlayerInteractEntity(event: PlayerInteractEntityEvent) {
        fire(Util.getHandNBT(event.player), Action.PLAYER_INTERACT_ENTITY, event.player, event)
    }

    @EventHandler
    fun onShearEntity(event: PlayerShearEntityEvent) {
        val player = event.player
        val data: PersistentDataContainer = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.SHEAR_ENTITY, player, event)
    }

    @EventHandler
    fun onBlockShearEntity(event: BlockShearEntityEvent) {
        val data: PersistentDataContainer = event.tool.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.BLOCK_SHEAR_ENTITY, null, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerTeleport(event: PlayerTeleportEvent) {
        fire(Util.getAllEquipmentNBT(event.player), Action.PLAYER_TELEPORT, event.player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOWEST)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        fire(Util.getAllEquipmentNBT(event.player), Action.PLAYER_QUIT, event.player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGH)
    fun onPlayerJoin(event: PlayerJoinEvent) {
        fire(Util.getAllEquipmentNBT(event.player), Action.PLAYER_JOIN, event.player, event)
    }

    @AllSlots
    @EventHandler
    fun onPlayerPickupExp(event: PlayerPickupExperienceEvent) {
        fire(Util.getAllEquipmentNBT(event.player), Action.PLAYER_PICKUP_EXP, event.player, event)
    }

    // ONLY FIRES IF PERSISTENT DATA IS IN THE ITEMSTACK!!!!
    @EventHandler
    fun onEntityPickupItem(event: EntityPickupItemEvent) {
        fire(event.item.itemStack.persistentDataContainer, Action.ENTITY_PICKUP_ITEM, null, event)
    }

    // Called when a hopper or hopper minecart picks up a dropped item.
    @EventHandler
    fun onHopperPickupEvent(event: InventoryPickupItemEvent) {
        fire(event.item.itemStack.persistentDataContainer, Action.HOPPER_PICKUP_ITEM, null, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onInventoryClickEvent(event: InventoryClickEvent) {
        val player = event.whoClicked as? Player ?: return
        val data = mutableListOf<PersistentDataContainer>()
        event.currentItem?.itemMeta?.persistentDataContainer.let { data.add(it ?: return) }
        event.cursor.itemMeta?.persistentDataContainer.let { data.add(it ?: return) }
        fire(data, Action.INVENTORY_CLICK, player, event)
    }

    @EventHandler
    fun onPlayerFillBucket(event: PlayerBucketFillEvent) {
        val player = event.player
        val data: PersistentDataContainer = event.itemStack?.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.FILL_BUCKET, player, event)
    }

    @EventHandler
    fun onPlayerBucketEmpty(event: PlayerBucketEmptyEvent) {
        val player = event.player
        val data = when (event.hand) {
            EquipmentSlot.HAND -> player.inventory.itemInMainHand.itemMeta?.persistentDataContainer
            EquipmentSlot.OFF_HAND -> player.inventory.itemInOffHand.itemMeta?.persistentDataContainer
            else -> return
        } ?: return
        fire(data, Action.EMPTY_BUCKET, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onPlayerPickupItem(event: PlayerAttemptPickupItemEvent) {
        val player = event.player
        val action = Action.PICKUP_ITEM
        if (!action.canFireRightNow()) return

        fire(Util.getAllEquipmentNBT(player), action, player, event, true)
    }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true) // if you modify this event to include previous slot, items that use this need a conditional added to their logic
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        val player = event.player
        val datas = listOfNotNull(
            player.inventory.getItem(event.newSlot)?.itemMeta?.persistentDataContainer,
            player.inventory.getItem(event.previousSlot)?.itemMeta?.persistentDataContainer
        ).ifEmpty { return }

        fire(datas, Action.ITEM_HELD, player, event)
    }

    @EventHandler
    fun onPlayerItemDamage(event: PlayerItemDamageEvent) {
        val player = event.player
        val data: PersistentDataContainer = event.item.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.ITEM_DAMAGE, player, event)
    }

    @EventHandler
    fun onEntityKnockbackByEntity(event: EntityKnockbackByEntityEvent) {
        val player = event.hitBy as? Player ?: return
        val data: PersistentDataContainer = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.PLAYER_KNOCKBACK_ENTITY, player, event)
    }

    @EventHandler
    fun onItemMerge(event: ItemMergeEvent) {
        val player = event.target.thrower?.let { Bukkit.getPlayer(it) } ?: return
        val data = event.target.persistentDataContainer
        fire(data, Action.ITEM_MERGE, player, event)
    }

    @EventHandler
    fun onSmashAttack(event: EntityAttemptSmashAttackEvent) {
        val player = event.entity as? Player ?: return
        val data: PersistentDataContainer = player.inventory.itemInMainHand.itemMeta?.persistentDataContainer ?: return

        fire(data, Action.MACE_SMASH_ATTACK, player, event)
    }

    @AllSlots
    //@EventHandler Unused
    fun onPlayerResurrect(event: EntityResurrectEvent) {
        val player = event.entity as? Player ?: return
        fire(player.equipmentContainers(), Action.PLAYER_RESURRECT, player, event)
    }

    @AllSlots
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onEntityExhaustion(event: EntityExhaustionEvent) {
        val player = event.entity as? Player ?: return
        fire(Util.getAllEquipmentNBT(player), Action.ENTITY_EXHAUSTION, player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        val player = event.view.player as? Player ?: return
        val datas = event.inventory.matrix
            .mapNotNull { it?.itemMeta?.persistentDataContainer }
            .ifEmpty { return }
        fire(datas, Action.PREPARE_CRAFT, player, event)
    }

    @EventHandler
    fun onEntityCompostItem(event: EntityCompostItemEvent) {
        val data: PersistentDataContainer = event.item.itemMeta?.persistentDataContainer ?: return
        fire(data, Action.ENTITY_COMPOST_ITEM, event.entity as? Player, event)
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCraftItem(event: CraftItemEvent) {
        val player = event.whoClicked as? Player ?: return
        val datas = event.inventory.matrix
            .mapNotNull { it?.itemMeta?.persistentDataContainer }
            .ifEmpty { return }
        fire(datas, Action.CRAFT_ITEM, player, event)
    }
}