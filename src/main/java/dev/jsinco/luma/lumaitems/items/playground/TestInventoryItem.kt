package dev.jsinco.luma.lumaitems.items.playground

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
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