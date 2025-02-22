package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.disabling.Ignore
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Ignore
class MoonshineFallowItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#6af7fb&lM&#6bf8e3&lo&#6cfaca&lo&#6dfbb2&ln&#6dfc99&ls&#71f291&lh&#79d0a8&li&#82afbf&ln&#8a8ed6&le &#926ced&lF&#a360ee&la&#b75ee4&ll&#cc5cda&ll&#e05bcf&lo&#f559c5&lw",
            mutableListOf("&#6aa4fbS&#a591fap&#e07ef9e&#fa88d9e&#f3af9bd &#ecd55dI"),
            mutableListOf("&#6aa4fb\"&#77a0fbI &#849cfam&#9098faa&#9d94fad&#aa90fae &#b78bf9i&#c387f9t &#d083f9f&#dd7ff9r&#ea7bf8o&#f777f8m &#fc79f1m&#fb82e4o&#f98ad6o&#f892c9n &#f69bbbs&#f5a3aeh&#f3aba0a&#f2b493r&#f0bc85d&#efc478s&#edcd6a!&#ecd55d\"","","§fWhile holding in your hand, you will gain","§fstackable speed"),
            Material.DIAMOND_HOE,
            mutableListOf("moonshinefallow"),
            mutableMapOf(Enchantment.EFFICIENCY to 7, Enchantment.FORTUNE to 5, Enchantment.UNBREAKING to 8, Enchantment.MENDING to 1)
        )
        //item.attributeModifiers[Attribute.MOVEMENT_SPEED] = AttributeModifier(UUID.randomUUID(), "generic.movementSpeed", 0.025, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
        //item.attributeModifiers[Attribute.ATTACK_SPEED] = AttributeModifier(UUID.randomUUID(), "generic.attackSpeed", 0.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
        //item.attributeModifiers[Attribute.ATTACK_DAMAGE] = AttributeModifier(UUID.randomUUID(), "generic.attackDamage", 0.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND)
        return Pair("moonshinefallow", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false // Do nothing
    }
}