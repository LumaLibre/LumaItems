package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import java.util.Random
import java.util.function.Consumer

@Disable(WorldName.EVENT_NEW)
class PeepPlushyMattockItem : CustomItem {

    companion object {
        private val oreColors: Map<Material, Color> = mapOf(
            Material.COAL to Color.fromRGB(33, 34, 31),
            Material.RAW_COPPER to Color.fromRGB(179, 92, 62),
            Material.LAPIS_LAZULI to Color.fromRGB(16, 67, 169),
            Material.RAW_IRON to Color.fromRGB(216, 175, 147),
            Material.RAW_GOLD to Color.fromRGB(191, 154, 31),
            Material.REDSTONE to Color.fromRGB(171, 1, 3),
            Material.DIAMOND to Color.fromRGB(124, 208, 200),
            Material.EMERALD to Color.fromRGB(43, 156, 82),
            Material.GOLD_NUGGET to Color.fromRGB(191, 154, 31),
            Material.QUARTZ to Color.fromRGB(185, 160, 154),
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#aca1ff&lP&#bbabff&le&#cab4ff&le&#dabeff&lp &#e9c7ff&lP&#f8d1ff&ll&#fdcaf6&lu&#febbe9&ls&#feacdc&lh&#fe9ccf&ly &#ff8dc2&lM&#ff81b4&la&#ff7aa4&lt&#ff7493&lt&#ff6d83&lo&#ff6773&lc&#ff6063&lk",
            mutableListOf("&#ACA1FFSnuggly Luck"),
            mutableListOf("Mining ores with this pickaxe grants","a chance to substantially increase","the amount of materials gained","","However, there is a chance for","mined ores to drop nothing"),
            Material.NETHERITE_PICKAXE,
            mutableListOf("peepplushymattock"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 10, Enchantment.EFFICIENCY to 8, Enchantment.FORTUNE to 5)
        )
        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"
        return Pair("peepplushymattock", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.BREAK_BLOCK -> {
                event as BlockBreakEvent
                event.isDropItems = snugglyLuck(event.block, event.block.drops)
            }
            else -> return false
        }
        return true
    }

    private fun snugglyLuck(block: Block, drops: Collection<ItemStack>): Boolean {
        if (!block.type.toString().endsWith("_ORE")) return true
        val chance = Random().nextInt(100)
        if (chance <= 17) {
            val dropMaterial = drops.iterator().next().type
            drops.forEach(Consumer { drop: ItemStack -> drop.amount *= 8 })
            for (i in drops.indices) {
                block.world.dropItemNaturally(block.location, drops.iterator().next())
            }
            block.world.playSound(block.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 1f, 1f)
            block.world.spawnParticle(Particle.DUST, block.location, 35, 0.5, 0.5, 0.5, 0.1, DustOptions(oreColors[dropMaterial] ?: return true, 1f))
            return false
        } else if (chance <= 24) {
            drops.forEach(Consumer { drop: ItemStack -> drop.amount = 0 })
            block.world.playSound(block.location, Sound.BLOCK_END_PORTAL_SPAWN, 0.2f, 1f)
            return false
        }
        return true
    }
}