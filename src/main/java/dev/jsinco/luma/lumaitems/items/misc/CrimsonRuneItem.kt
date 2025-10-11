package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.PaperDataComponent
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class CrimsonRuneItem : CustomItemFunctions() {

    companion object {
        private val EFFECT = PotionEffect(PotionEffectType.FIRE_RESISTANCE, 250, 0, false, false, true)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#CE5F03>C<#C95609>r<#C44E10>i<#BF4516>m<#BA3C1C>s<#C65B1B>o<#D17B1B>n <#D4831B>R<#CC6B1B>u<#C3541C>n<#BA3C1C>e")
            .material(Material.NETHER_BRICK)
            .lore(
                "<gray>A rune made from crimson clay,",
                "<gray>baked in the sun for an age.",
                "",
                "<gray>It grants its holder immunity",
                "<gray>from the heat."
            )
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .persistentData("crimson-rune-item")
            .tier(Tier.SUMMER_2025)
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        player.addPotionEffect(EFFECT)
    }
}
