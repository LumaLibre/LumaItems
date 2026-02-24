package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW, hard = true)
class MiniPoppyItem : CustomItemFunctions() {

    private val key = "mini-poppy"

    override fun createItem(): Pair<String, ItemStack> {

        return ItemFactory.builder()
            .name("<b><#FF5959>M<#FF6259>i<#FF6B58>n<#FF765F>i <#FE8D6C>P<#FD8A84>o<#FB869C>p<#FC7890>p<#FC6984>y")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .customEnchants("<#ff5959>Flower Extract")
            .lore(
                "A wonderful smelling poppy,",
                "It's so small and cute!",
                "",
                "I wonder if holding it",
                "does anything special?"
            )
            .tier(Tier.VALENTIDE_2026) // valentide 2026
            .persistentData(key)
            .material(Material.POPPY)
            .attributeModifiers(
                AttributeContainer.of(key, Attribute.SCALE, AttributeModifier.Operation.ADD_NUMBER, -0.5, EquipmentSlotGroup.ANY),
                //AttributeContainer.of(key, Attribute.GRAVITY , AttributeModifier.Operation.ADD_NUMBER, -0.05, EquipmentSlotGroup.ANY),
                //AttributeContainer.of(key, Attribute.SAFE_FALL_DISTANCE, AttributeModifier.Operation.ADD_NUMBER, 6.0, EquipmentSlotGroup.ANY)
            )
            .autoHat(true)
            .buildPair()
    }


    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (!item.isMatchingItem(key)) {
            return
        }
        event.isCancelled = true
    }
}