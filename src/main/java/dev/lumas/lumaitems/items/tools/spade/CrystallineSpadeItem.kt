package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.tags.Kind
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class CrystallineSpadeItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#FF5A5A:#FF8539:#FCDB61:#A1DE4B:#1DC680:#2ED4B4:#71C6E0:#8B79D6>Crystalline Spade</gradient></b>")
            .customEnchants("<#8B79D6>Style")
            .material(Material.DIAMOND_SHOVEL)
            .tier(Tier.CHRISTMAS_2025)
            .persistentData("crystalline-spade")
            .lore(
                //"",
                "Broken <#8B79D6>gravel</#8B79D6> or <#8B79D6>clay</#8B79D6>",
                "will yield randomly",
                "colored glass rather",
                "than their respective",
                "drops."
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 7,
                Enchantment.UNBREAKING to 10,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (block.type == Material.GRAVEL || block.type == Material.CLAY) {
            block.world.dropItemNaturally(block.location, ItemStack(Kind.COLORED_GLASS.values.random()))
            event.isDropItems = false
        }
    }
}