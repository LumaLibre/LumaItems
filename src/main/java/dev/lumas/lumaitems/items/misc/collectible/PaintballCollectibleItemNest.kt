package dev.lumas.lumaitems.items.misc.collectible

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.model.PaperDataComponent
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.util.tiers.Tier
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Disable(WorldName.EVENT_NEW, WorldName.SPAWN, WorldName.PINATA, hard = true)
abstract class PaintballCollectibleItem(
    private val paintball: Material,
    private val paint: Material,
    private val persistentData: String,
    private val name: String,
    private val lore: List<String>
) : CustomItemFunctions() {

    private val nameSpace = Util.namespacedKey(persistentData)
    private val paintballItem = ItemStack(paintball)
    private val paintBlockData = paint.createBlockData()
    private val customItem = createItem().second

    final override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name(name)
            .lore(lore.toMutableList())
            .material(Material.SNOWBALL)
            .persistentData(nameSpace)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .tier(Tier.SUMMER_2025)
            .paperDataComponents(
                PaperDataComponent.valued(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(paintball.name.lowercase()))
            )
            .attributeModifiers(
                AttributeContainer.of(nameSpace, Attribute.BLOCK_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, -4.5, EquipmentSlotGroup.HAND),
            )
            .buildPair()
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val entity = event.entity
        if (Util.hasPersistentKey(entity, nameSpace)) return
        event.isCancelled = true
        val snowball = player.world.createEntity(entity.location, Snowball::class.java)
        Util.setPersistentKey(snowball, nameSpace, PersistentDataType.SHORT, 1)
        snowball.shooter = player
        snowball.velocity = entity.velocity
        snowball.item = paintballItem
        snowball.spawnAt(entity.location)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        if (event.hitEntity != null) {
            event.isCancelled = true
            return
        }

        val hitBlock = event.hitBlock ?: return
        player.sendBlockChange(hitBlock.location, paintBlockData)
    }

}

class BluePaintballCollectibleItem : PaintballCollectibleItem(
    Material.BLUE_DYE,
    Material.BLUE_WOOL,
    "blue-paintball-collectible",
    "<b><gradient:#669CFF:#0D4AEF>Blue Paintball</gradient></b>",
    listOf(
        "<gray>A pristine vial of paint,",
        "<gray>fresh from the Paintball",
        "<gray>battlegrounds."
    )
)

class RedPaintballCollectibleItem : PaintballCollectibleItem(
    Material.RED_DYE,
    Material.RED_WOOL,
    "red-paintball-collectible",
    "<b><gradient:#ff6666:#EF0D0D>Red Paintball</gradient></b>",
    listOf(
        "<gray>The ultimate red. Embodies",
        "<gray>every fierce splat from",
        "<gray>the paintball minigame."
    )
)