package dev.lumas.lumaitems.items.tools.shears

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockShearEntityEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class RainbowShearsItem : CustomItem {

    companion object {
        val woolTypes: List<Material> = listOf(Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL,
            Material.YELLOW_WOOL, Material.LIME_WOOL, Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL,
            Material.PURPLE_WOOL, Material.BLUE_WOOL, Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ff6666:#ffbd55:#ffff66:#9de24f:#87cefa>Rainbow</gradient></b> <b><white>Shears</white></b>")
            .lore(
                "<gray>Sheared mobs will drop extra,",
                "<gradient:#ff6666:#ffbd55:#ffff66:#9de24f:#87cefa>rainbow</gradient> <gray>colors of wool."
            )
            .persistentData("rainbowshears")
            .vanillaEnchants(
                Enchantment.UNBREAKING to 6,
                Enchantment.EFFICIENCY to 7,
                Enchantment.MENDING to 1
            )
            .quotes("<gray>\"Shear to your heart's content!\"")
            .material(Material.SHEARS)
            .tier(Tier.PRIDE_2025)
            .buildPair()
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.SHEAR_ENTITY -> {
                event as PlayerShearEntityEvent
                val drops: MutableList<ItemStack> = event.drops.toMutableList()
                for (i in 0..Random.nextInt(4)) {
                    drops.add(ItemStack(woolTypes.random()))
                }
                event.drops = drops
            }
            Action.BLOCK_SHEAR_ENTITY -> {
                event as BlockShearEntityEvent
                val drops: MutableList<ItemStack> = event.drops.toMutableList()
                for (i in 0..Random.nextInt(4)) {
                    drops.add(ItemStack(woolTypes.random()))
                }
                event.drops = drops
            }
            else -> return false
        }
        return true
    }
}