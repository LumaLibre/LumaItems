package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import java.util.function.Consumer
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class MagmaticShovelItem : CustomItemFunctions() {
    override fun createItem() = ItemFactory.builder()
        .name("<b><gradient:#f52a2a:#eb9f5b:#cd0b0b>Magmatic Shovel</gradient></b>")
        .customEnchants("<#f52a2a>Smelt")
        .lore("Converts <#f52a2a>mined</#f52a2a> sand",
            "into glass.")
        .material(Material.NETHERITE_SHOVEL)
        .persistentData("magmatic-shovel")
        .vanillaEnchants(
            Enchantment.EFFICIENCY to 8,
            Enchantment.UNBREAKING to 10,
            Enchantment.MENDING to 1,
            Enchantment.SILK_TOUCH to 1
        )
        .tier(Tier.LUMARINE_2026)
        .buildPair()

    @Suppress("DEPRECATION")
    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val blockBroken = event.block
        val drops = event.block.getDrops(player.inventory.itemInMainHand)
        if (blockBroken.type == Material.SAND || blockBroken.type == Material.RED_SAND) {
            drops.forEach(Consumer { drop: ItemStack ->
                if (drop.type == Material.SAND || drop.type == Material.RED_SAND)  {
                    drop.type = Material.GLASS
                }
            })
            for (i in drops.indices) {
                blockBroken.world.dropItem(blockBroken.location.toCenterLocation(), drops.iterator().next())
            }
        }
    }
}