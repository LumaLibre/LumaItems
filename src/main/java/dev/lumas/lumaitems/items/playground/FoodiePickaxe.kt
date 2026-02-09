package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
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
            .autoHat(true)
            .build()
            .createItem()

        item.setData(DataComponentTypes.MAX_DAMAGE, 100)
        return Pair("foodie_pickaxe", item)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}