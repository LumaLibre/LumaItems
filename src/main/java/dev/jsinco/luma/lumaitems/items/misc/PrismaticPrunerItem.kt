package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerShearEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class PrismaticPrunerItem : CustomItemFunctions() {

    companion object {
        val wools: List<Material> = listOf(
            Material.WHITE_WOOL, Material.ORANGE_WOOL, Material.MAGENTA_WOOL, Material.LIGHT_BLUE_WOOL, Material.YELLOW_WOOL, Material.LIME_WOOL,
            Material.PINK_WOOL, Material.GRAY_WOOL, Material.LIGHT_GRAY_WOOL, Material.CYAN_WOOL, Material.PURPLE_WOOL, Material.BLUE_WOOL,
            Material.BROWN_WOOL, Material.GREEN_WOOL, Material.RED_WOOL, Material.BLACK_WOOL
        )
        val key = Util.namespacedKey("wooltype")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#C18DF5>P<#C784E9>r<#CD7BDE>i<#D372D2>s<#D969C7>m<#DF5FBB>a<#E556AF>t<#EB4DA4>i<#F14498>c <#DC6F8B>P<#D28485>r<#C89A7E>u<#BDAF78>n<#B3C471>e<#A8DA6B>r<#9EEF64>s</b>")
            .lore("Sheared mobs will drop", "any wool of your choice.", " ", "Shift + Right click to change wool color.")
            .material(Material.SHEARS)
            .vanillaEnchants(Enchantment.UNBREAKING to 6, Enchantment.EFFICIENCY to 7, Enchantment.MENDING to 1)
            .tier(Tier.WINTER_2024)
            .persistentData("prismaticpruner")
            .stringPersistentDatas(mutableMapOf(key to Material.WHITE_WOOL.name))
            .buildPair()

    }

    private fun getActiveWoolType(itemStack: ItemStack): Material {
        val woolType = itemStack.itemMeta?.persistentDataContainer?.get(key, PersistentDataType.STRING) ?: return Material.WHITE_WOOL
        return Material.valueOf(woolType)

    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {

        if (!player.isSneaking) return

        val item = player.inventory.itemInMainHand
        val woolChoice = wools.indexOf(getActiveWoolType(item))
        val nextWool = (woolChoice + 1) % wools.size

        val newMeta = item.itemMeta?.apply { persistentDataContainer.set(key, PersistentDataType.STRING, wools[nextWool].name) }
            ?: return
        item.itemMeta = newMeta

        player.sendActionBar(MiniMessageUtil.mm("<#f498f6>${wools[nextWool].name}"))
    }

    override fun onShearEntity(player: Player, event: PlayerShearEntityEvent) {
        val woolType = getActiveWoolType(player.inventory.itemInMainHand)
        event.drops = mutableListOf(ItemStack(woolType, random().nextInt(1,4)))
    }

}