package dev.lumas.lumaitems.items.misc.scale

import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.syncDelayed
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Consumable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.event.entity.EntityCompostItemEvent
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack

class GrowBerriesItem : CustomItemFunctions() {

    private companion object {
        val KEY = "grow-berries".namespacedKey()
        const val DURATION_TICKS = 60L * 20L

        val GROWN_ATTRIBUTES = listOf(
            Attribute.SCALE,
            Attribute.MAX_HEALTH,
            Attribute.MOVEMENT_SPEED,
            Attribute.JUMP_STRENGTH,
            Attribute.SAFE_FALL_DISTANCE,
            Attribute.BLOCK_INTERACTION_RANGE,
            Attribute.ENTITY_INTERACTION_RANGE,
            Attribute.OXYGEN_BONUS,
            Attribute.STEP_HEIGHT,
        )
    }

    private val activeTimers: MutableMap<UUID, ScheduledTask> = ConcurrentHashMap()

    @Suppress("UnstableApiUsage")
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory.builder()
            .name("<b><gradient:#8BC34A:#C8E6A0:#66BB6A>Grow Berries</gradient></b>")
            .customEnchants("<#8BC34A>Gigantify")
            .material(Material.GLOW_BERRIES)
            .persistentData(KEY)
            .tier(Tier.LUMARINE_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .maxStackSize(1)
            .lore(
                "Plump berries bursting",
                "with growth.",
                "",
                "<#8BC34A>Eat</#8BC34A> them to grow twice",
                "as big for one minute:",
                "doubled size, speed,",
                "health, and more.",
            )
            .build()
            .createItem()

        item.setData(DataComponentTypes.CONSUMABLE, Consumable.consumable())
        val food = FoodProperties.food().canAlwaysEat(true).nutrition(0).saturation(0f)
        item.setData(DataComponentTypes.FOOD, food)
        return Pair("grow-berries", item)
    }

    override fun onConsumeItem(player: Player, event: PlayerItemConsumeEvent) {
        if (!event.item.isMatchingItem(KEY)) return
        event.isCancelled = true

        val healthFraction = player.getAttribute(Attribute.MAX_HEALTH)
            ?.let { if (it.value > 0) player.health / it.value else 1.0 } ?: 1.0

        activeTimers.remove(player.uniqueId)?.cancel()
        clearGrownModifiers(player)
        applyGrownModifiers(player)

        player.getAttribute(Attribute.MAX_HEALTH)?.let {
            player.health = (healthFraction * it.value).coerceIn(0.0, it.value)
        }

        player.world.playSound(player.location, Sound.ITEM_BONE_MEAL_USE, 1f, 0.7f)
        player.world.playSound(player.location, Sound.BLOCK_SWEET_BERRY_BUSH_PICK_BERRIES, 1f, 0.8f)

        player.syncDelayed(DURATION_TICKS) {
            clearGrownModifiers(player)
            activeTimers.remove(player.uniqueId)
            player.world.playSound(player.location, Sound.ITEM_BOTTLE_EMPTY, 1f, 1.2f)
        }?.let { activeTimers[player.uniqueId] = it }
    }

    private fun applyGrownModifiers(player: Player) {
        for (attribute in GROWN_ATTRIBUTES) {
            val instance = player.getAttribute(attribute) ?: continue
            val modifier = AttributeContainer.builder(KEY)
                .setAttribute(attribute)
                .setOperation(AttributeModifier.Operation.ADD_SCALAR)
                .setAmount(1.0) // +100% of base -> twice as much
                .build()
                .modifier()
            instance.addTransientModifier(modifier)
        }
    }

    private fun clearGrownModifiers(player: Player) {
        for (attribute in GROWN_ATTRIBUTES) {
            val instance = player.getAttribute(attribute) ?: continue
            instance.modifiers
                .filter { it.key == KEY }
                .forEach { instance.removeModifier(it) }
        }
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        if (!event.itemInHand.isMatchingItem(KEY)) return
        event.isCancelled = true
    }

    override fun onEntityCompostItem(event: EntityCompostItemEvent) {
        if (!event.item.isMatchingItem(KEY)) return
        event.isCancelled = true
    }
}
