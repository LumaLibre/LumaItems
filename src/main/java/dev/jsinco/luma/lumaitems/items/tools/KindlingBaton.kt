package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import dev.jsinco.luma.lumaitems.util.tiers.ThanksgivingEventTier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class KindlingBaton : CustomItemFunctions() {

    companion object {
        private val smeltTypes = mapOf(
            Material.GOLD_ORE to Material.GOLD_INGOT,
            Material.DEEPSLATE_GOLD_ORE to Material.GOLD_INGOT,
            Material.NETHER_GOLD_ORE to Material.GOLD_INGOT,
            Material.IRON_ORE to Material.IRON_INGOT,
            Material.DEEPSLATE_IRON_ORE to Material.IRON_INGOT,
            Material.COPPER_ORE to Material.COPPER_INGOT,
            Material.DEEPSLATE_COPPER_ORE to Material.COPPER_INGOT,
            Material.ANCIENT_DEBRIS to Material.NETHERITE_SCRAP,
            Material.SAND to Material.GLASS,
            Material.RED_SAND to Material.RED_STAINED_GLASS
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#F3AB59>K<#E9A461>i<#E09C6A>n<#D69572>d<#CC8D7A>l<#D6A088>i<#E0B297>n<#EBC5A5>g <#FFEAC2>B<#E4DFB9>a<#C8D5B0>t<#ADCAA6>o<#91BF9D>n</b>")
            .customEnchants("<#F3AB59>Oven")
            .persistentData("kindlingbaton")
            .material(Material.NETHERITE_PICKAXE)
            .tier(ThanksgivingEventTier.THANKSGIVING_2024)
            .lore("Automatically smelts mined", "ores and sand blocks.", "", "Tap your <#645B82>swap <white>key", "to alternate tool types.")
            .vanillaEnchants(Enchantment.FORTUNE to 4, Enchantment.EFFICIENCY to 7, Enchantment.UNBREAKING to 6, Enchantment.MENDING to 1)
            .buildPair()
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        event.isCancelled = true
        val item = player.inventory.itemInMainHand

        if (item.type == Material.NETHERITE_PICKAXE) {
            item.type = Material.NETHERITE_SHOVEL
        } else if (item.type == Material.NETHERITE_SHOVEL) {
            item.type = Material.NETHERITE_PICKAXE
        }
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {

        val block = event.block
        if (!smeltTypes.contains(block.type)) return
        event.isDropItems = false

        val drops = block.getDrops(player.inventory.itemInMainHand);
        for (drop in drops) {
            drop.type = smeltTypes[block.type] ?: continue
        }
        //block.world.spawn(block.location, ExperienceOrb::class.java).experience = 1
        for (i in drops.indices) {
            event.block.world.dropItemNaturally(event.block.location, drops.elementAt(i))
        }
    }
}
