package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItem
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class SeagullFeatherItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&b&lSeagull Feather",
            mutableListOf(),
            mutableListOf("This beautiful seagull feather","shines in the light!", "", "Holding this feather in your", "offhand will give a speed boost."),
            Material.FEATHER,
            mutableListOf("seagullfeather"),
            mutableMapOf(Enchantment.UNBREAKING to 10)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        item.addAttributeContainer(AttributeContainer.of("seagullfeather", Attribute.MOVEMENT_SPEED, AttributeModifier.Operation.ADD_NUMBER, 0.025, EquipmentSlotGroup.OFFHAND))
        return Pair("seagullfeather", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}
