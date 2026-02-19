package dev.lumas.lumaitems.items.armor.chestplate

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class StarweaveAegisItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#917afb&lS&#8984f8&lt&#808ef5&la&#7899f2&lr&#6fa3ee&lw&#67adeb&le&#5eb7e8&la&#56c2e5&lv&#4dcce2&le &#45d6df&lA&#3ce0db&le&#34ebd8&lg&#2bf5d5&li&#23ffd2&ls",
            mutableListOf("&#6fa3eeS&#60b5e8t&#51c8e3a&#41daddt&#32edd8u&#23ffd2e"),
            mutableListOf("&#8b81f9\"&#8786f7T&#828cf6i&#7e91f4m&#7a96f3e &#759bf1i&#71a1efn &#6da6eem&#68abeco&#64b0eat&#60b6e9i&#5bbbe7o&#57c0e6n&#53c5e4, &#4ecbe2y&#4ad0e1o&#46d5dfu &#41daddi&#3de0dcn &#39e5das&#34ead9t&#30efd7o&#2cf5d5n&#27fad4e&#23ffd2\"","","&fWearing this chestplate will make","&fyou immune to all knockback effects"),
            Material.NETHERITE_CHESTPLATE,
            mutableListOf("starweaveaegis"),
            mutableMapOf(Enchantment.PROTECTION to 7, Enchantment.FIRE_PROTECTION to 5, Enchantment.PROJECTILE_PROTECTION to 5, Enchantment.BLAST_PROTECTION to 5, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1)
        )
//        item.attributeContainers[Attribute.ARMOR] = AttributeModifier(UUID.randomUUID(), "generic.armor", 8.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST)
//        item.attributeContainers[Attribute.KNOCKBACK_RESISTANCE] = AttributeModifier(UUID.randomUUID(), "generic.knockbackResistance", 1000.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST)
//        item.attributeContainers[Attribute.ARMOR_TOUGHNESS] = AttributeModifier(UUID.randomUUID(), "generic.armorToughness", 3.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.CHEST)
        return Pair("starweaveaegis", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false // No abilities
    }
}