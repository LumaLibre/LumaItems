package dev.lumas.lumaitems.items.armor.chestplate

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import org.bukkit.Material
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