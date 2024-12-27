package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
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
        val item = ItemFactory(
            "&#EC6248&lR&#EBBE5F&la&#C5E895&li&#7CE1EA&ln&#668AF0&lb&#745DEC&lo&#A558DD&lw &f&lShears",
            mutableListOf(),
            mutableListOf("Sheared mobs will drop extra,", "rainbow colors of wool."),
            Material.SHEARS,
            mutableListOf("rainbowshears"),
            mutableMapOf(Enchantment.UNBREAKING to 6, Enchantment.EFFICIENCY to 7, Enchantment.MENDING to 1)
        )
        item.addQuote("&7\"Shear to your heart's content!\"")
        item.tier = "&#731385&lP&#4332B9&lr&#1351ED&li&#0C6A87&ld&#058221&le &#7FB715&l2&#F9EB08&l0&#EF7A05&l2&#E40902&l4"
        return Pair("rainbowshears", item.createItem())
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