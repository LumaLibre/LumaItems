package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class HeartyShroomItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#7c0b03:#9b101e:#bd2a2a:#fffdd3:#fff5bd:#ffe9a1>Hearty Shroom</gradient></b>")
            .material(Material.RED_MUSHROOM)
            .persistentData("hearty-shroom")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(hideEnchants = true)
            .tier(Tier.SUMMER_2025)
            .attributeModifiers(
                AttributeContainer.of("hearty-shroom", Attribute.MAX_HEALTH, AttributeModifier.Operation.ADD_NUMBER, 4.0, EquipmentSlotGroup.ANY),
            )
            .lore(
                "A mushroom with a rich",
                "scent. Holding it out will",
                "boost your number of",
                "<#bd2a2a>hearts</#bd2a2a>."
            )
            .buildPair()
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        event.isCancelled = true
    }
}