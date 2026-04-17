package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.shapes.ShapeUtil
import dev.lumas.lumaitems.util.tags.Kind
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class VentanasMattockItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#302045:#523966:#c580aa:#8080BC:#ffbd9a:#ffe2b6>Ventana's Mattock</gradient></b>")
            .customEnchants("<gradient:#c580aa:#8080BC>Destructive")
            .material(Material.NETHERITE_PICKAXE)
            .tier(Tier.WONDERLAND_2026.alt())
            .persistentData("ventanas-mattock")
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
        val block = event.block

        val item = player.inventory.itemInMainHand

        if (block.getDestroySpeed(item) == Float.POSITIVE_INFINITY) {
            return
        }

        val corner1 = block.location.add(1.0, 1.0, 1.0)
        val corner2 = block.location.add(-1.0, -1.0, -1.0)
        ShapeUtil.getCuboidBlocks(corner1, corner2).filter {
            !Kind.BLACKLIST.isTagged(it.type) && it.isSolid
        }.forEach {
            it.breakNaturallyWithLog(player, item, true)
        }
    }
}