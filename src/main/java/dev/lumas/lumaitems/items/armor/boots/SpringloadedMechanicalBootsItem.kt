package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class SpringloadedMechanicalBootsItem : CustomItemFunctions() {
    // Feats:
    // - Allows the player to jump higher than normal
    // - Allows the player to step 2 blocks high
    // - Magnetic items?

    // Some Shovel:
    // charges up and when released, randomly selects blocks around the player, makes them glow, and makes them explode in megumin explosion style
    override fun createItem(): Pair<String, ItemStack> {
        val key = "sunset-loafers"
        return ItemFactory.builder()
            .name("<b><gradient:#fad39d:#d9c8ba:#c7a9b6:#8e9fc4>Sunset Loafers</gradient></b>")
            .persistentData(key)
            .material(Material.NETHERITE_BOOTS)
            .buildPair()
        //DataComponentTypes
        //Attribute
    }




}