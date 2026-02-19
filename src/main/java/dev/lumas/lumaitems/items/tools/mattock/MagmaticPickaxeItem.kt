package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import java.util.function.Consumer
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ExperienceOrb
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class MagmaticPickaxeItem : CustomItem {

    companion object {
        var smeltOreTypes = listOf(
            Material.GOLD_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.NETHER_GOLD_ORE,
            Material.IRON_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.ANCIENT_DEBRIS
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#f52a2a&lV&#f43b31&lo&#f24b38&ll&#f15c3f&lc&#ef6d46&la&#ee7e4d&ln&#ec8e54&li&#eb9f5b&lc &#e78a50&lP&#e27544&li&#de6039&lc&#da4a2d&lk&#d63522&la&#d12016&lx&#cd0b0b&le",
            mutableListOf("&#f1892aH&#f27d2ae&#f3722aa&#f4662at&#f55a2bi&#f54e2bn&#f6432bg &#f7372bU&#f82b2bp"),
            mutableListOf("§fConverts mined gold, copper,","§fand iron ore to ingots"),
            Material.NETHERITE_PICKAXE,
            mutableListOf("magmaticpickaxe"),
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1, Enchantment.SILK_TOUCH to 1, Enchantment.FIRE_ASPECT to 4)
        )
        return Pair("magmaticpickaxe", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val blockBreakEvent: BlockBreakEvent? = event as? BlockBreakEvent

        when (type) {
            Action.BREAK_BLOCK -> {
                heatingUp(blockBreakEvent!!.block, blockBreakEvent.block.getDrops(player.inventory.itemInMainHand))
            }
            else -> return false
        }
        return true
    }

    private fun heatingUp(blockBroken: Block, drops: Collection<ItemStack>) {
        if (!smeltOreTypes.contains(blockBroken.type)) return
        drops.forEach(Consumer { drop: ItemStack ->
            when (drop.type) {
                Material.GOLD_ORE, Material.DEEPSLATE_GOLD_ORE, Material.NETHER_GOLD_ORE -> drop.setType(Material.GOLD_INGOT)
                Material.IRON_ORE, Material.DEEPSLATE_IRON_ORE -> drop.setType(Material.IRON_INGOT)
                Material.COPPER_ORE, Material.DEEPSLATE_COPPER_ORE -> drop.setType(Material.COPPER_INGOT)
                Material.ANCIENT_DEBRIS -> drop.setType(Material.NETHERITE_SCRAP)
                else -> return@Consumer
            }
            blockBroken.world.spawn(blockBroken.location, ExperienceOrb::class.java).experience = 1
            for (i in drops.indices) {
                blockBroken.world.dropItemNaturally(blockBroken.location, drops.iterator().next())
            }
        })
    }
}