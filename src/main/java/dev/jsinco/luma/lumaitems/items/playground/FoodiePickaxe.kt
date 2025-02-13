package dev.jsinco.luma.lumaitems.items.playground

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItem
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


@Suppress("UnstableApiUsage")
class FoodiePickaxe : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory.builder()
            .name("<green>Foodie Pickaxe")
            .persistentData(mutableListOf("foodie_pickaxe"))
            .material(Material.NETHERITE_AXE)
            .tier("<gold>Legendary")
            .vanillaEnchants(Enchantment.LURE to 2)
            .build()
            .createItem()

        item.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey("discordnitroset", "discordnitro_axe"))
        return Pair("foodie_pickaxe", item)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}