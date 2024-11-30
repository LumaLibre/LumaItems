package dev.jsinco.lumaitems.items.tools

import dev.jsinco.lumaitems.items.ItemFactory
import dev.jsinco.lumaitems.enums.Action
import dev.jsinco.lumaitems.manager.CustomItem
import dev.jsinco.lumaitems.util.disabling.Disable
import dev.jsinco.lumaitems.util.disabling.WorldName
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.util.Random
import java.util.function.Consumer

@Disable(WorldName.EVENT_NEW)
class MoonstoneMattockItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#477edf&lM&#5f8ae3&lo&#7797e7&lo&#8fa3ec&ln&#a7b0f0&ls&#bfbcf4&lt&#b7b4f3&lo&#b0acf2&ln&#a8a5f0&le &#a19def&lM&#9995ee&la&#9f94e7&lt&#a593e0&lt&#ab93d9&lo&#b192d2&lc&#b791cb&lk",
            mutableListOf("&#90add9M&#98b3dbo&#a0b9ddo&#a8bfdfn &#b0c5e1& &#b9cbe4S&#c1d1e6h&#c9d7e8i&#d1ddean&#d9e3ece"),
            mutableListOf("§fMining ores with this pickaxe grants","§fa chance to substantially increase the","§famount of materials gained","","§fHowever, there is a chance for mined","§for ores to drop nothing"),
            Material.NETHERITE_PICKAXE,
            mutableListOf("moonstonemattock"),
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1, Enchantment.FORTUNE to 5)
        )
        return Pair("moonstonemattock", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val blockBreakEvent: BlockBreakEvent? = event as? BlockBreakEvent

        when (type) {
            Action.BREAK_BLOCK -> {
                blockBreakEvent!!.isDropItems = gamblersRemark(blockBreakEvent.block, blockBreakEvent.block.drops)
            }
            else -> return false
        }
        return true
    }

    private fun gamblersRemark(block: Block, drops: Collection<ItemStack>): Boolean {
        if (!block.type.toString().lowercase().contains("ore")) return false
        val chance = Random().nextInt(100)
        if (chance <= 25) {
            drops.forEach(Consumer { drop: ItemStack -> drop.amount *= 10 })
            block.world.spawnParticle(Particle.TOTEM_OF_UNDYING, block.location, 50, 0.5, 0.5, 0.5, 0.1)
            block.world.playSound(block.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 1f, 1f)
            for (i in drops.indices) {
                block.world.dropItemNaturally(block.location, drops.iterator().next())
            }
            return true
        } else if (chance <= 40) {
            drops.forEach(Consumer { drop: ItemStack -> drop.amount = 0 })
            block.world.playSound(block.location, Sound.BLOCK_END_PORTAL_SPAWN, 0.2f, 1f)
            return true
        }
        return false
    }
}