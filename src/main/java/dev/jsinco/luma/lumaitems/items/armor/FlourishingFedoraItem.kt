package dev.jsinco.luma.lumaitems.items.armor

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.shapes.Sphere
import dev.jsinco.luma.lumaitems.util.NeedsEdits
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@NeedsEdits
class FlourishingFedoraItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#7ECB7A>F<#8CC888>l<#9AC495>o<#A8C1A3>u<#B6BEB0>r<#C5BABE>i<#D3B7CB>s<#E1B4D9>h<#EFB0E6>i<#FDADF4>n<#EFB0F3>g <#D4B5F1>F<#C7B8F0>e<#B9BBEF>d<#ABBEEE>o<#9EC0ED>r<#90C3EC>a</b>")
            .lore(
                "Nearby crops will grow faster",
                "while wearing this helmet."
            )
            .material(Material.NETHERITE_HELMET)
            .tier(Tier.WINTER_2024)
            .quotes("<#90C3EC>\"<#8EC4E1>I<#8CC5D5>t <#89C6BE>g<#87C7B3>r<#85C8A8>o<#83C99C>w<#82C991>s <#7ECB7A>o<#8CC888>n <#A8C1A3>y<#B6BEB0>o<#C5BABE>u<#D3B7CB>.<#E1B4D9>.<#EFB0E6>.<#FDADF4>\"")
            .vanillaEnchants(
                Enchantment.UNBREAKING to 10,
                Enchantment.PROTECTION to 6,
                Enchantment.THORNS to 8,
                Enchantment.MENDING to 1
            )
            .persistentData("flourishing-fedora")
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        // Make nearby crops grow faster

        if (random().nextInt(100) > 5) {
            return
        }

        val sphere = Sphere(player.location, 6.0, 20.0)
        val crops = sphere.sphere.filter { !it.isEmpty && it.blockData is Ageable }

        crops.forEach {
            val crop = it.blockData as Ageable
            if (crop.age == crop.maximumAge) return@forEach
            crop.age += 1
            it.blockData = crop
            it.state.update()
        }
    }
}