package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.disabling.Ignore
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockShearEntityEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.inventory.ItemStack

@Ignore
class PrismaticPrunerItem : CustomItem {

    companion object {
        val wools: List<Material> = listOf(
            Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
            Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
            Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#C18DF5>P<#C784E9>r<#CD7BDE>i<#D372D2>s<#D969C7>m<#DF5FBB>a<#E556AF>t<#EB4DA4>i<#F14498>c <#DC6F8B>P<#D28485>r<#C89A7E>u<#BDAF78>n<#B3C471>e<#A8DA6B>r<#9EEF64>s</b>")
            .lore("Sheared mobs will drop", "any wool of your choice.", " ", "Shift + Right click to change wool color.")
            .material(Material.SHEARS)
            .vanillaEnchants(Enchantment.UNBREAKING to 6, Enchantment.EFFICIENCY to 7, Enchantment.MENDING to 1)
            .tier(Tier.WINTER_2024)
            .persistentData("prismaticpruner")
            .buildPair()

    }

    private var woolChoice: Int = 0
    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                event as PlayerInteractEvent

                if (player.isSneaking) {
                    woolChoice++
                    if (woolChoice >= wools.size) {
                        woolChoice = 0
                    }

                    player.sendMessage("Wool type set to: ${wools[woolChoice].name}")
                }
            }

            Action.SHEAR_ENTITY -> {
                event as PlayerShearEntityEvent
                val drops: MutableList<ItemStack> = event.drops.toMutableList()

                for (i in 0 until 2) {
                    drops.add(ItemStack(wools[woolChoice]))
                }

                event.drops = drops
            }

            Action.BLOCK_SHEAR_ENTITY -> {
                event as BlockShearEntityEvent
                val drops: MutableList<ItemStack> = event.drops.toMutableList()

                for (i in 0 until 2) {
                    drops.add(ItemStack(wools[woolChoice]))
                }

                event.drops = drops
            }
            else -> return false
        }
        return true
    }

}