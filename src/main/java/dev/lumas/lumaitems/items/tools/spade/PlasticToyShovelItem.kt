package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItem
import kotlin.random.Random
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack

class PlasticToyShovelItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#EF3F43&lP&#DB4756&ll&#C74E6A&la&#B3567D&ls&#9F5E90&lt&#8A65A4&li&#766DB7&lc&#6275CA&l &#4E7CDE&lT&#3A84F1&lo&#499EBE&ly&#59B88B&l &#68D257&lS&#77EC24&lh&#94E22E&lo&#B2D838&lv&#CFCE42&le&#ECC44C&ll",
            mutableListOf("&#766DB7Abundant"),
            mutableListOf("When breaking sand or gravel,", "this shovel offers an extra", "chance to triple drops."),
            Material.NETHERITE_SHOVEL,
            mutableListOf("plastictoyshovel"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 9, Enchantment.EFFICIENCY to 8, Enchantment.FORTUNE to 3)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        return Pair("plastictoyshovel", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.BLOCK_DROP_ITEM -> {
                event as BlockDropItemEvent

                if (Random.nextInt(100) > 8) {
                    return false
                }

                event.items.forEach {
                    val item = it.itemStack
                    if (item.type == Material.SAND || item.type == Material.RED_SAND || item.type == Material.GRAVEL) {
                        val block = event.block
                        block.world.spawnParticle(Particle.DUST, block.location.add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1,
                            Particle.DustOptions(block.blockData.mapColor, 1f)
                        )
                        item.amount *= 3
                        return true
                    }
                }
            }

            else -> return false
        }
        return false
    }
}