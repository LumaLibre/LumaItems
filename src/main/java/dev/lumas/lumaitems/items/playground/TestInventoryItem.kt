package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class TestInventoryItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("hello")
            .customEnchants(mutableListOf("<gray>Unbreakable"))
            .material(Material.FISHING_ROD)
            .persistentData("hello")
            .vanillaEnchants(mutableMapOf(Enchantment.LURE to 3))
            .tier(Tier.CARNIVAL_2024)
            .unbreakable(true)
            .buildPair()
    }

    override fun onInventoryClick(player: Player, event: InventoryClickEvent) {
        val altItem: ItemStack =
        if (event.currentItem?.persistentDataContainer?.has(NamespacedKey(instance(), "hello")) == true) {
            event.cursor
        } else {
            event.currentItem ?: ItemStack(Material.AIR)
        }


        player.sendMessage("You clicked the item!: ${altItem.type}")
    }
}