package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.util.tiers.Tier
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
                "hearts."
            )
            .buildPair()
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        event.isCancelled = true
    }
}