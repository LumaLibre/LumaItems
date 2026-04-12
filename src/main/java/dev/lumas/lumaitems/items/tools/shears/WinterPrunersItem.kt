package dev.lumas.lumaitems.items.tools.shears

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class WinterPrunersItem : CustomItemFunctions() {

    companion object {
        private val PATTERN = Regex(".*(GLASS|_HEAD|_WALL_HEAD|SKELETON_SKULL)")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#556ba2:#9490b5:#d2b8be:#4f899f:#39748f>Winter Pruners</gradient></b>")
            .customEnchants("<#9490b5>Fast Clipper")
            .material(Material.SHEARS)
            .tier(Tier.CHRISTMAS_2025)
            .persistentData("winter-pruners")
            .lore(
                "<#9490b5>Left-click</#9490b5> to quickly",
                "shear through glass",
                "or decorative heads."
            )
            .vanillaEnchants(
                Enchantment.UNBREAKING to 6,
                Enchantment.EFFICIENCY to 7,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        if (PATTERN.matches(block.type.name)) {
            player.breakBlock(block)
        }
    }
}