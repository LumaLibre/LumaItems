package dev.jsinco.luma.lumaitems.items.armor.trousers

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack

class WitchingStocksItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        AttributeModifier.Operation.ADD_NUMBER
        return ItemFactory.builder()
            .name("<b><gradient:#320343:#5a2d3e:#876541:#8da649:#74eb62>Witching Stocks</gradient></b>")
            .customEnchants("<gold>Reach II")
            .attributeModifiers(
                AttributeContainer.builder()
                    .setKey("witching-stocks")
                    .setAttribute(org.bukkit.attribute.Attribute.ENTITY_INTERACTION_RANGE)
                    .setSlot(org.bukkit.inventory.EquipmentSlotGroup.LEGS)
                    .setOperation(AttributeModifier.Operation.ADD_SCALAR)
                    .setAmount(0.5)
                    .build()
            )
            .persistentData("witching-stocks")
            .material(org.bukkit.Material.NETHERITE_LEGGINGS)
            .buildPair()
    }
}