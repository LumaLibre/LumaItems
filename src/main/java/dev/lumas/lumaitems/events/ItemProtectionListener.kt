package dev.lumas.lumaitems.events

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.util.extensions.isCollectible
import dev.lumas.lumaitems.util.extensions.isLumaItem
import dev.lumas.lumaitems.util.extensions.isProtected
import dev.lumas.lumaitems.util.extensions.setRemainingHealth
import dev.lumas.lumaitems.util.extensions.willBreak
import io.papermc.paper.event.entity.EntityCompostItemEvent
import java.lang.reflect.Modifier
import org.bukkit.Keyed
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag
import org.bukkit.block.Block
import org.bukkit.block.Container
import org.bukkit.entity.Item
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockCookEvent
import org.bukkit.event.block.BlockDispenseEvent
import org.bukkit.event.block.CrafterCraftEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.EntityResurrectEvent
import org.bukkit.event.inventory.BrewEvent
import org.bukkit.event.inventory.BrewingStandFuelEvent
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.inventory.PrepareAnvilEvent
import org.bukkit.event.inventory.PrepareGrindstoneEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.inventory.PrepareSmithingEvent
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe

/**
 * Keeps protected items from being consumed, destroyed, or turned into vanilla items
 * This runs after the events in Listeners, so all the abilities should still work
 */
@Register(Autowire.LISTENER)
class ItemProtectionListener : Listener {

    private companion object {

        // Should cover breeding, taming, healing and feeding
        @Suppress("UNCHECKED_CAST")
        private val ANIMAL_FOOD: Set<Material> = Tag::class.java.fields
            .filter { Modifier.isStatic(it.modifiers) && it.name.startsWith("ITEMS_") && it.name.endsWith("_FOOD") }
            .mapNotNull { it.get(null) as? Tag<Material> }
            .flatMapTo(HashSet()) { it.values }

        private val CONSUMED_ON_ENTITY: Set<Material> = buildSet {
            addAll(ANIMAL_FOOD)
            addAll(Tag.ITEMS_DYES.values)
            addAll(Material.entries.filter { it.name.endsWith("_SPAWN_EGG") })
            add(Material.BONE)
            add(Material.NAME_TAG)
            add(Material.LEAD)
            add(Material.BUCKET)
            add(Material.BOWL)
        }

        private val CONSUMED_WORKSTATION_SLOT: Map<InventoryType, Int> = mapOf(
            InventoryType.CARTOGRAPHY to 1, // paper, glass pane, or empty map
            InventoryType.STONECUTTER to 0, // input block
            InventoryType.LOOM to 1 // dye
        )

        // Right-clicking these with a collectible is harmless (containers are checked separately)
        private val SAFE_INTERACT_BLOCKS: Set<Material> = buildSet {
            listOf(
                Tag.DOORS, Tag.TRAPDOORS, Tag.FENCE_GATES, Tag.BUTTONS, Tag.BEDS,
                Tag.ANVIL, Tag.WOODEN_SHELVES
            ).forEach { addAll(it.values) }
            addAll(listOf(
                Material.CHISELED_BOOKSHELF, Material.LEVER, Material.ENDER_CHEST, Material.CRAFTING_TABLE,
                Material.ENCHANTING_TABLE, Material.SMITHING_TABLE, Material.GRINDSTONE, Material.STONECUTTER,
                Material.LOOM, Material.CARTOGRAPHY_TABLE, Material.BEACON, Material.BELL, Material.NOTE_BLOCK,
                Material.REPEATER, Material.COMPARATOR, Material.DAYLIGHT_DETECTOR
            ))
        }
    }


    // ---- Collectibles only ----

    @EventHandler(priority = EventPriority.LOWEST)
    fun onCollectibleInteract(event: PlayerInteractEvent) {
        if (!event.action.isRightClick || !event.item.isCollectible()) return
        event.setUseItemInHand(Event.Result.DENY)
        if (event.clickedBlock?.isSafeToUse() == true) return
        event.setUseInteractedBlock(Event.Result.DENY) // cauldrons, composters, campfires, flower pots, etc.
    }

    private fun Block.isSafeToUse(): Boolean {
        return type in SAFE_INTERACT_BLOCKS || getState(false) is Container
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onCollectibleInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.rightClicked is ItemFrame) return
        if (!event.player.inventory.getItem(event.hand).isCollectible()) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onCollectibleConsume(event: PlayerItemConsumeEvent) {
        if (event.item.isCollectible()) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onCollectibleResurrect(event: EntityResurrectEvent) {
        val hand = event.hand ?: return
        val item = event.entity.equipment?.getItem(hand) ?: return
        if (item.isCollectible()) event.isCancelled = true
    }

    //@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    fun onCollectibleDamage(event: PlayerItemDamageEvent) {
        val item = event.item
        if (!item.isCollectible() || !item.willBreak(event.damage)) return
        item.setRemainingHealth(1)
        event.isCancelled = true
    }


    // ---- Collectibles and Luma items ----

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBlockDispenseItem(event: BlockDispenseEvent) {
        if (event.block.type != Material.DISPENSER || !event.item.isProtected()) return
        event.isCancelled = true // buckets, bone meal, etc.
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onInteractEntity(event: PlayerInteractEntityEvent) {
        if (event.rightClicked is ItemFrame) return
        val item = event.player.inventory.getItem(event.hand)
        if (item.type !in CONSUMED_ON_ENTITY || !item.isProtected()) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onCompost(event: EntityCompostItemEvent) {
        if (event.item.isProtected()) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onMobPickup(event: EntityPickupItemEvent) {
        if (event.entity is Player || !event.item.itemStack.isProtected()) return
        event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onItemEntityDamage(event: EntityDamageEvent) {
        val item = event.entity as? Item ?: return
        if (event.cause == EntityDamageEvent.DamageCause.VOID || !item.itemStack.isProtected()) return
        event.isCancelled = true // Lava, fire, cactus, explosions, etc.
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onCook(event: BlockCookEvent) {
        if (event.source.isProtected()) event.isCancelled = true // All types of furnaces and campfires
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onFurnaceFuel(event: FurnaceBurnEvent) {
        if (event.fuel.isProtected()) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBrew(event: BrewEvent) {
        if (event.contents.ingredient.isProtected()
            || event.contents.contents.any { item -> item.isProtected() }) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onBrewingFuel(event: BrewingStandFuelEvent) {
        if (event.fuel.isProtected()) event.isCancelled = true
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPrepareCraft(event: PrepareItemCraftEvent) {
        if (consumesProtected(event.recipe, event.inventory.matrix, event.inventory.result)) {
            event.inventory.result = null
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onCrafterCraft(event: CrafterCraftEvent) {
        val ingredients = (event.block.state as? Container)?.snapshotInventory?.contents ?: return
        if (consumesProtected(event.recipe, ingredients, event.result)) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPrepareAnvil(event: PrepareAnvilEvent) {
        if (event.inventory.secondItem.isProtected()) event.result = null
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPrepareGrindstone(event: PrepareGrindstoneEvent) {
        val inventory = event.inventory
        if (inventory.upperItem.isProtected() || inventory.lowerItem.isProtected()) event.result = null
    }

    @EventHandler(priority = EventPriority.HIGH)
    fun onPrepareSmithing(event: PrepareSmithingEvent) {
        val inventory = event.inventory
        if (consumesProtected(inventory.recipe, arrayOf(inventory.inputTemplate, inventory.inputMineral), event.result)) {
            event.result = null
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    fun onWorkstationTake(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        if (event.slotType != InventoryType.SlotType.RESULT) return
        val consumedSlot = CONSUMED_WORKSTATION_SLOT[inventory.type] ?: return
        if (inventory.getItem(consumedSlot).isProtected()) event.isCancelled = true
    }

    // Whether crafting this recipe would use up a protected item and result in an unprotected item
    private fun consumesProtected(recipe: Recipe?, ingredients: Array<out ItemStack?>, result: ItemStack?): Boolean {
        if (result == null || result.isProtected()) return false
        val vanilla = isVanillaRecipe(recipe)
        return ingredients.any { it.isCollectible() || (vanilla && it?.isLumaItem() == true) }
    }

    private fun isVanillaRecipe(recipe: Recipe?): Boolean {
        return (recipe as? Keyed)?.key?.namespace == NamespacedKey.MINECRAFT
    }
}
