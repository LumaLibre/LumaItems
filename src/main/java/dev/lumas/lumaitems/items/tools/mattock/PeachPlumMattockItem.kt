package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItem
import kotlin.random.Random
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class PeachPlumMattockItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#f08080&lP&#f18784&le&#f28d87&la&#f4948b&lc&#f59a8e&lh &#f6a192&lP&#f6a892&ll&#f6af92&lu&#f6b692&lm &#f6bd92&lM&#f6c492&la&#f6c692&lt&#f6c892&lt&#f6cb92&lo&#f6cd92&lc&#f6cf92&lk",
            mutableListOf("&#f18784Delightful Bounty"),
            mutableListOf("When mining ores with this", "mattock, occasionally, ores will", "transform into their full block"),
            Material.NETHERITE_PICKAXE,
            mutableListOf("peachplummattock"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.EFFICIENCY to 8, Enchantment.UNBREAKING to 10, Enchantment.SILK_TOUCH to 1)
        )
        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"

        return Pair("peachplummattock", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.LEFT_CLICK -> {
                if (Random.nextInt(500) > 4) return false
                event as PlayerInteractEvent
                var blockName = event.clickedBlock?.type?.name ?: return false
                if (blockName.endsWith("_ORE") && !blockName.contains("NETHER_")) {
                    blockName = blockName.replace("_ORE", "_BLOCK")
                        .replace("DEEPSLATE_", "").trim()
                }
                event.clickedBlock?.type = Material.valueOf(blockName)
            }
            else -> return false
        }
        return true
    }
}