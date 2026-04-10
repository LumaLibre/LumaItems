package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.shapes.ShapeUtil
import dev.lumas.lumaitems.util.Kind
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class VentanasSpadeItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#302045:#523966:#c580aa:#8080BC:#ffbd9a:#ffe2b6>Ventana's Spade</gradient></b>")
            .customEnchants("<gradient:#c580aa:#8080BC>Destructive")
            .material(Material.NETHERITE_SHOVEL)
            .tier(Tier.WONDERLAND_2026.alt())
            .persistentData("ventanas-spade")
            .lore(
                "<gradient:#c580aa:#8080BC>Breaks</gradient> blocks in a",
                "3x3 radius.",
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
        val item = player.inventory.itemInMainHand
        val block = event.block

        if (block.getDestroySpeed(item) == Float.POSITIVE_INFINITY) {
            return
        }

        val corner1 = block.location.add(1.0, 1.0, 1.0)
        val corner2 = block.location.add(-1.0, -1.0, -1.0)
        ShapeUtil.getCuboidBlocks(corner1, corner2).filter {
            Tag.MINEABLE_SHOVEL.isTagged(it.type) && !Kind.BLACKLIST.isTagged(it.type) && it.isSolid
        }.forEach {
            it.breakNaturallyWithLog(player, item, true)
        }
    }
}