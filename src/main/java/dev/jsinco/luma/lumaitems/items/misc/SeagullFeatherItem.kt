package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import java.util.UUID

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
        // AttributeModifier(@NotNull NamespacedKey key, double amount, @NotNull Operation operation, @NotNull EquipmentSlotGroup slot)
        item.attributeModifiers[Attribute.MOVEMENT_SPEED] = AttributeModifier(UUID.randomUUID(), "movementSpeed", 0.025, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.OFF_HAND)
        return Pair("seagullfeather", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}
