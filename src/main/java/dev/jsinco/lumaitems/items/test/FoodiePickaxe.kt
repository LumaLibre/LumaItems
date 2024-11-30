package dev.jsinco.lumaitems.items.test

import dev.jsinco.lumaitems.enums.Action
import dev.jsinco.lumaitems.items.ItemFactory
import dev.jsinco.lumaitems.manager.CustomItem
import io.papermc.paper.command.brigadier.argument.ArgumentTypes.itemStack
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Equippable
import io.papermc.paper.datacomponent.item.FoodProperties
import io.papermc.paper.registry.keys.SoundEventKeys
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
            .material(Material.NETHERITE_CHESTPLATE)
            .tier("<gold>Legendary")
            .vanillaEnchants(Enchantment.LURE to 2)
            .build()
            .createItem()

        val food: FoodProperties.Builder = FoodProperties.food()
            .canAlwaysEat(true)
            .nutrition(2)
            .saturation(3.5f)


        item.editMeta {
            it.isGlider = true
        }

        item.setData(DataComponentTypes.FOOD, food)
        return Pair("foodie_pickaxe", item)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false
    }
}