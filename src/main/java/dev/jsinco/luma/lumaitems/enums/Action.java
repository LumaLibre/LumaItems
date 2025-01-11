package dev.jsinco.luma.lumaitems.enums;

/**
 * Enum for different actions that can be performed by LumaItems listeners.
 * This enum is expanded as events are added.
 */
public enum Action {

    /**
     * Global actions that effect every player. Called from LumaItems main class or a runnable.
     */
    RUNNABLE, // When the global LumaItems runnable calls. Tick rate is every 70 ticks. This runnable is in sync with the main thread and is thread safe.
    ASYNC_RUNNABLE, // When the global LumaItems runnable calls. Tick rate is every 30 ticks. This runnable is async and is not thread safe.
    PLUGIN_ENABLE, // When the plugin is enabled
    PLUGIN_DISABLE, // When the plugin is disabled

    /**
     * External actions that must be called by listeners provided by another plugin.
     */
    JOBS_EXP_GAIN, // When a player gains experience from Jobs Reborn
    JOBS_PRE_PAYMENT, // When a player is about to be paid from Jobs Reborn

    /**
     * Player actions that effect a specific player. Called by a listener.
     */
    CROSSBOW_LOAD, // When a player loads a crossbow in their main hand
    PROJECTILE_LAUNCH, // When a player launches a projectile from their main or offhand
    PROJECTILE_LAND, // When a projectile that was shot by a player and has a specific persistent data lands
    RIGHT_CLICK, // When a player right-clicks an item
    LEFT_CLICK, // When a player left-clicks an item
    GENERIC_INTERACT, // A generic interaction (I believe this is called when a player crouches while holding an item?)
    SWAP_HAND, // When a player swaps their main and offhand items (F Key)
    ENTITY_DEATH, // When a living entity dies and the killer is a player
    ENTITY_DAMAGE, // When a player or a projectile shot by a player damages a living entity
    PLAYER_DAMAGED_BY_ENTITY, // When a player is damaged by a living entity
    PLAYER_DAMAGED_WHILE_BLOCKING, // When a player is damaged while blocking with a shield
    PLAYER_DAMAGE_GENERIC, // When a player damages themselves <-- FOR REMOVAL
    ENTITY_DAMAGED_GENERIC, // When a living entity is damaged from ANYTHING and not just other living entities
    DROP_ITEM, // When a player drops an item
    BREAK_BLOCK, // When a player breaks a block
    LOW_PRIO_BREAK_BLOCK, // When a player breaks a block, but with a lower priority
    CACHED_BLOCK_BREAK, // When a player breaks a block and the block is cached <-- Edit description
    BLOCK_DROP_ITEM, // After a player breaks a block, and the block drops item(s)
    PLACE_BLOCK, // When a player places a block
    FISH, // When a player fishes in their main or offhand
    ELYTRA_BOOST, // When a player boosts themselves with an item while gliding with an elytra
    PLAYER_CROUCH, // When a player toggles their sneak button (crouches or un-crouches)
    ASYNC_CHAT, // When a player sends a chat message (Async)
    MOVE, // When a player moves and their position/location has changed
    ENTITY_MOVE, // When a living entity with specific persistent data moves and their position/location has changed
    CONSUME_ITEM, // When a player consumes an item
    JUMP, // When a player jumps (Unused)
    ENTITY_CHANGE_BLOCK, // When an entity changes a block (Unused)
    POTION_EFFECT, // When a player is affected by a potion effect
    ENTITY_TARGET_PLAYER, // When a living entity with a specific persistent data targets a player
    ARMOR_CHANGE, // When a player changes their armor
    ENTITY_TELEPORT, // When a living entity with a specific persistent data teleports
    PLAYER_INTERACT_ENTITY, // When a player interacts at a living entity (Unused, No listener)
    //INVENTORY_CLICK, // When a player clicks in their inventory
    SHEAR_ENTITY, // When a player shears a living entity
    BLOCK_SHEAR_ENTITY, // When a block shears a living entity
    PLAYER_TELEPORT, // When a player teleports
    PLAYER_QUIT, // When a player quits the server
    PLAYER_PICKUP_EXP, // When a player picks up experience
    MACE_SMASH_ATTACK, // When a player performs a smash attack with a mace
    ENTITY_PICKUP_ITEM, // When a living entity data picks up an item with specific persistent data
    HOPPER_PICKUP_ITEM, // When a hopper or hopper minecart picks up an item with specific persistent data
    INVENTORY_CLICK, // When a player clicks an item in their inventory with a specific persistent data
}
