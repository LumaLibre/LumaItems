package dev.jsinco.luma.lumaitems.manager

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import com.destroystokyo.paper.event.player.PlayerElytraBoostEvent
import com.destroystokyo.paper.event.player.PlayerJumpEvent
import com.destroystokyo.paper.event.player.PlayerPickupExperienceEvent
import com.gamingmesh.jobs.api.JobsExpGainEvent
import com.gamingmesh.jobs.api.JobsPrePaymentEvent
import dev.jsinco.luma.lumaitems.enums.Action
import io.papermc.paper.event.entity.EntityLoadCrossbowEvent
import io.papermc.paper.event.entity.EntityMoveEvent
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.BlockShearEntityEvent
import org.bukkit.event.entity.EntityChangeBlockEvent
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.EntityTargetLivingEntityEvent
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.event.player.PlayerBucketEmptyEvent
import org.bukkit.event.player.PlayerBucketFillEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.event.player.PlayerToggleSneakEvent

/**
 * Abstract class which gives a dedicated function for each action at the cost of added overhead.
 */
abstract class CustomItemFunctions : CustomItem {

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        // Should always be exhaustive
        when(type) {
            Action.RUNNABLE -> onRunnable(player)
            Action.ASYNC_RUNNABLE -> onAsyncRunnable(player)
            Action.PLUGIN_ENABLE -> onPluginEnable()
            Action.PLUGIN_DISABLE -> onPluginDisable()
            Action.JOBS_EXP_GAIN -> onJobsExpGain(player, event as JobsExpGainEvent)
            Action.JOBS_PRE_PAYMENT -> onJobsPrePayment(player, event as JobsPrePaymentEvent)
            Action.CROSSBOW_LOAD -> onCrossBowLoad(player, event as EntityLoadCrossbowEvent)
            Action.PLAYER_SHOOT_BOW -> onPlayerShootBow(player, event as EntityShootBowEvent)
            Action.PROJECTILE_LAUNCH -> onProjectileLaunch(player, event as ProjectileLaunchEvent)
            Action.PROJECTILE_LAND -> onProjectileLand(player, event as ProjectileHitEvent)
            Action.RIGHT_CLICK -> onRightClick(player, event as PlayerInteractEvent)
            Action.LEFT_CLICK -> onLeftClick(player, event as PlayerInteractEvent)
            Action.GENERIC_INTERACT -> onGenericInteract(player, event as PlayerInteractEvent)
            Action.SWAP_HAND -> onPlayerSwapHands(player, event as PlayerSwapHandItemsEvent)
            Action.ENTITY_DEATH -> onEntityDeath(player, event as EntityDeathEvent)
            Action.ENTITY_DAMAGE -> onEntityDamage(player, event as EntityDamageByEntityEvent)
            Action.PLAYER_DAMAGED_BY_ENTITY -> onPlayerDamagedByEntity(player, event as EntityDamageByEntityEvent)
            Action.PLAYER_DAMAGED -> onPlayerDamaged(player, event as EntityDamageEvent)
            Action.ENTITY_DAMAGED_GENERIC -> onEntityDamageGeneric(player, event as EntityDamageEvent)
            Action.DROP_ITEM -> onPlayerDropItem(player, event as PlayerDropItemEvent)
            Action.BREAK_BLOCK -> onBreakBlock(player, event as BlockBreakEvent)
            Action.LOW_PRIO_BREAK_BLOCK -> onLowPriorityBlockBreak(player, event as BlockBreakEvent)
            Action.CACHED_BLOCK_BREAK -> onCachedBlockBreak(player, event as BlockBreakEvent)
            Action.BLOCK_DROP_ITEM -> onBlockDropItem(player, event as BlockDropItemEvent)
            Action.PLACE_BLOCK -> onPlaceBlock(player, event as BlockPlaceEvent)
            Action.FISH -> onFish(player, event as PlayerFishEvent)
            Action.ELYTRA_BOOST -> onElytraBoost(player, event as PlayerElytraBoostEvent)
            Action.PLAYER_CROUCH -> onPlayerCrouch(player, event as PlayerToggleSneakEvent)
            Action.ASYNC_CHAT -> onAsyncChat(player, event as AsyncPlayerChatEvent)
            Action.MOVE -> onMove(player, event as PlayerMoveEvent)
            Action.INPUT -> onInput(player, event as PlayerInputEvent)
            Action.ENTITY_MOVE -> onEntityMove(event as EntityMoveEvent)
            Action.CONSUME_ITEM -> onConsumeItem(player, event as PlayerItemConsumeEvent)
            Action.JUMP -> onJump(player, event as PlayerJumpEvent)
            Action.ENTITY_CHANGE_BLOCK -> onEntityChangeBlock(event as EntityChangeBlockEvent)
            Action.POTION_EFFECT -> onPotionEffect(player, event as EntityPotionEffectEvent)
            Action.ENTITY_TARGET_PLAYER -> onEntityTargetPlayer(player, event as EntityTargetLivingEntityEvent)
            Action.ARMOR_CHANGE -> onArmorChange(player, event as PlayerArmorChangeEvent)
            Action.ENTITY_TELEPORT -> onEntityTeleport(event as EntityTeleportEvent)
            Action.PLAYER_INTERACT_ENTITY -> onPlayerInteractEntity(player, event as PlayerInteractAtEntityEvent)
            Action.SHEAR_ENTITY -> onShearEntity(player, event as PlayerShearEntityEvent)
            Action.BLOCK_SHEAR_ENTITY -> onBlockShearEntity(event as BlockShearEntityEvent)
            Action.PLAYER_TELEPORT -> onPlayerTeleport(player, event as PlayerTeleportEvent)
            Action.PLAYER_QUIT -> onPlayerQuit(player, event as PlayerQuitEvent)
            Action.PLAYER_PICKUP_EXP -> onPlayerPickupExp(player, event as PlayerPickupExperienceEvent)
            Action.MACE_SMASH_ATTACK -> onMaceSmashAttack(player, event as EntityDamageByEntityEvent)
            Action.ENTITY_PICKUP_ITEM -> onEntityPickupItem(event as EntityPickupItemEvent)
            Action.HOPPER_PICKUP_ITEM -> onHopperPickupItem(event as InventoryPickupItemEvent)
            Action.INVENTORY_CLICK -> onInventoryClick(player, event as InventoryClickEvent)
            Action.FILL_BUCKET -> onPlayerFillBucket(player, event as PlayerBucketFillEvent)
            Action.EMPTY_BUCKET -> onPlayerEmptyBucket(player, event as PlayerBucketEmptyEvent)
            Action.PICKUP_ITEM -> onPlayerPickupItem(player, event as PlayerAttemptPickupItemEvent)
            Action.ITEM_HELD -> onPlayerItemHeld(player, event as PlayerItemHeldEvent)
            Action.ITEM_DAMAGE -> onPlayerItemDamage(player, event as PlayerItemDamageEvent)
        }
        return true
    }



    open fun onRunnable(player: Player) {}
    open fun onAsyncRunnable(player: Player) {}
    open fun onPluginEnable() {}
    open fun onPluginDisable() {}
    open fun onJobsExpGain(player: Player, event: JobsExpGainEvent) {}
    open fun onJobsPrePayment(player: Player, event: JobsPrePaymentEvent) {}
    open fun onCrossBowLoad(player: Player, event: EntityLoadCrossbowEvent) {}
    open fun onPlayerShootBow(player: Player, event: EntityShootBowEvent) {}
    open fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {}
    open fun onProjectileLand(player: Player, event: ProjectileHitEvent) {}
    open fun onRightClick(player: Player, event: PlayerInteractEvent) {}
    open fun onLeftClick(player: Player, event: PlayerInteractEvent) {}
    open fun onGenericInteract(player: Player, event: PlayerInteractEvent) {}
    open fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {}
    open fun onEntityDeath(player: Player, event: EntityDeathEvent) {}
    open fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {}
    open fun onPlayerDamagedByEntity(player: Player, event: EntityDamageByEntityEvent) {}
    open fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {}
    open fun onEntityDamageGeneric(player: Player, event: EntityDamageEvent) {}
    open fun onPlayerDropItem(player: Player, event: PlayerDropItemEvent) {}
    open fun onBreakBlock(player: Player, event: BlockBreakEvent) {}
    open fun onLowPriorityBlockBreak(player: Player, event: BlockBreakEvent) {}
    open fun onCachedBlockBreak(player: Player, event: BlockBreakEvent) {}
    open fun onBlockDropItem(player: Player, event: BlockDropItemEvent) {}
    open fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {}
    open fun onFish(player: Player, event: PlayerFishEvent) {}
    open fun onElytraBoost(player: Player, event: PlayerElytraBoostEvent) {}
    open fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {}
    open fun onAsyncChat(player: Player, event: AsyncPlayerChatEvent) {}
    open fun onMove(player: Player, event: PlayerMoveEvent) {}
    open fun onInput(player: Player, event: PlayerInputEvent) {}
    open fun onEntityMove(event: EntityMoveEvent) {}
    open fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {}
    open fun onJump(player: Player, event: PlayerJumpEvent) {}
    open fun onEntityChangeBlock(event: EntityChangeBlockEvent) {}
    open fun onPotionEffect(player: Player, event: EntityPotionEffectEvent) {}
    open fun onEntityTargetPlayer(player: Player, event: EntityTargetLivingEntityEvent) {}
    open fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {}
    open fun onEntityTeleport(event: EntityTeleportEvent) {}
    open fun onPlayerInteractEntity(player: Player, event: PlayerInteractAtEntityEvent) {}
    open fun onShearEntity(player: Player, event: PlayerShearEntityEvent) {}
    open fun onBlockShearEntity(event: BlockShearEntityEvent) {}
    open fun onPlayerTeleport(player: Player, event: PlayerTeleportEvent) {}
    open fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {}
    open fun onPlayerPickupExp(player: Player, event: PlayerPickupExperienceEvent) {}
    open fun onMaceSmashAttack(player: Player, event: EntityDamageByEntityEvent) {}
    open fun onEntityPickupItem(event: EntityPickupItemEvent) {}
    open fun onHopperPickupItem(event: InventoryPickupItemEvent) {}
    open fun onInventoryClick(player: Player, event: InventoryClickEvent) {}
    open fun onPlayerFillBucket(player: Player, event: PlayerBucketFillEvent) {}
    open fun onPlayerEmptyBucket(player: Player, event: PlayerBucketEmptyEvent) {}
    open fun onPlayerPickupItem(player: Player, event: PlayerAttemptPickupItemEvent) {}
    open fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {}
    open fun onPlayerItemDamage(player: Player, event: PlayerItemDamageEvent) {}
}