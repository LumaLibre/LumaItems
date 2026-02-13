package dev.lumas.lumaitems.enums;

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
    FAST_ASYNC_RUNNABLE, // When the global LumaItems runnable calls. Tick rate is every 3 ticks. This runnable is async and is not thread safe.
    PLUGIN_ENABLE, // When the plugin is enabled
    PLUGIN_DISABLE, // When the plugin is disabled
    PLUGIN_DISABLE_GLOBAL,

    /**
     * External actions that must be called by listeners provided by another plugin.
     */
    JOBS_EXP_GAIN, // When a player gains experience from Jobs Reborn
    JOBS_PRE_PAYMENT, // When a player is about to be paid from Jobs Reborn
    MCMMO_TREE_FELLER_DESTROY_TREE, // When a player destroys a tree with the MCMMO Tree Feller ability

    /**
     * Player actions that effect a specific player. Called by a listener.
     */
    CROSSBOW_LOAD, // When a player loads a crossbow in their main hand
    PLAYER_SHOOT_BOW, // When a player shoots a bow
    PROJECTILE_LAUNCH, // When a player launches a projectile from their main or offhand
    PROJECTILE_LAND, // When a projectile that was shot by a player and has a specific persistent data lands
    RIGHT_CLICK, // When a player right-clicks an item
    LEFT_CLICK, // When a player left-clicks an item
    GENERIC_INTERACT, // A generic interaction (I believe this is called when a player crouches while holding an item?)
    SWAP_HAND, // When a player swaps their main and offhand items (F Key)
    ENTITY_DEATH, // When a living entity dies and the killer is a player
    PLAYER_DEATH, // When a player dies while wearing specific nbt
    ENTITY_DAMAGE, // When a player or a projectile shot by a player damages a living entity
    PLAYER_DAMAGED_BY_ENTITY, // When a player is damaged by a living entity
    //PLAYER_DAMAGED_WHILE_BLOCKING, // When a player is damaged while blocking with a shield
    PLAYER_DAMAGED, // When a player damages themselves <-- FOR REMOVAL
    ENTITY_DAMAGED_GENERIC, // When a living entity is damaged from ANYTHING and not just other living entities
    DROP_ITEM, // When a player drops an item
    BREAK_BLOCK, // When a player breaks a block
    LOW_PRIO_BREAK_BLOCK, // When a player breaks a block, but with a lower priority
    CACHED_BLOCK_BREAK, // When a player breaks a block and the block is cached <-- Edit description
    BLOCK_DROP_ITEM, // After a player breaks a block, and the block drops item(s)
    PLACE_BLOCK, // When a player places a block
    BLOCK_DAMAGE, // When a player damages a block (e.g. starts mining it)
    FISH, // When a player fishes in their main or offhand
    ELYTRA_BOOST, // When a player boosts themselves with an item while gliding with an elytra
    PLAYER_CROUCH, // When a player toggles their sneak button (crouches or un-crouches)
    ASYNC_CHAT, // When a player sends a chat message (Async)
    MOVE(true), // When a player moves and their position/location has changed
    INPUT(true), // When a key input is received from a player (e.g. pressing a key)
    ENTITY_MOVE(2, true), // When a living entity with specific persistent data moves and their position/location has changed
    CONSUME_ITEM, // When a player consumes an item
    JUMP, // When a player jumps
    ENTITY_FORM_BLOCK, // When an entity changes a block
    POTION_EFFECT, // When a player is affected by a potion effect
    ENTITY_TARGET_PLAYER, // When a living entity with a specific persistent data targets a player
    ARMOR_CHANGE, // When a player changes their armor
    ENTITY_TELEPORT, // When a living entity with a specific persistent data teleports
    PLAYER_INTERACT_AT_ENTITY, // When a player interacts at a living entity
    PLAYER_INTERACT_ENTITY, // When a player right-clicks a entity
    //INVENTORY_CLICK, // When a player clicks in their inventory
    SHEAR_ENTITY, // When a player shears a living entity
    BLOCK_SHEAR_ENTITY, // When a block shears a living entity
    PLAYER_TELEPORT, // When a player teleports
    PLAYER_QUIT, // When a player quits the server
    PLAYER_JOIN, // When a player joins the server
    PLAYER_PICKUP_EXP, // When a player picks up experience
    MACE_SMASH_ATTACK, // When a player performs a smash attack with a mace
    ENTITY_PICKUP_ITEM, // When a living entity data picks up an item with specific persistent data
    HOPPER_PICKUP_ITEM, // When a hopper or hopper minecart picks up an item with specific persistent data
    INVENTORY_CLICK, // When a player clicks an item in their inventory with a specific persistent data
    FILL_BUCKET, // When a player fills a bucket with a specific persistent data
    EMPTY_BUCKET, // When a player empties a bucket with a specific persistent data
    PICKUP_ITEM(10, true), // When a player picks up an item
    ITEM_HELD, // When a player holds an item
    ITEM_DAMAGE, // When an item is damaged
    PLAYER_KNOCKBACK_ENTITY, // When a player knocks back an entity
    ITEM_MERGE, // When an item with specific persistent data merges with another item
    PLAYER_RESURRECT, // When a player is resurrected (e.g. from a totem of undying) while wearing specific nbt

    ;

    private final int callSlowdown;
    private final boolean hot;

    Action(int callSlowdown, boolean hot) {
        this.callSlowdown = callSlowdown;
        this.hot = hot;
    }

    Action(int callSlowdown) {
        this.callSlowdown = callSlowdown;
        this.hot = false;
    }

    Action(boolean hot) {
        this.callSlowdown = 0;
        this.hot = hot;
    }

    Action() {
        this.callSlowdown = 0;
        this.hot = false;
    }

    public int getCallSlowdown() {
        return callSlowdown;
    }

    public boolean isHot() {
        return hot;
    }
}
