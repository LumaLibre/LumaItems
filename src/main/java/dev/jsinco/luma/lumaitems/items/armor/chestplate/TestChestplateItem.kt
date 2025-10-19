package dev.jsinco.luma.lumaitems.items.armor.chestplate

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.inventory.ItemStack

class TestChestplateItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Test Chestplate 2")
            .persistentData("test-chestplate")
            .material(Material.NETHERITE_CHESTPLATE)
            .buildPair()
    }

}