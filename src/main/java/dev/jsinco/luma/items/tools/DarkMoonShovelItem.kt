package dev.jsinco.luma.items.tools

import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import dev.jsinco.luma.util.disabling.Disable
import dev.jsinco.luma.util.disabling.WorldName
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class DarkMoonShovelItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#342f6c&lD&#46407b&la&#57528b&lr&#69639a&lk&#7b75aa&lm&#8c86b9&lo&#9e97c8&lo&#a89fcf&ln &#aa9cce&lS&#ac99cd&lh&#af97cc&lo&#b194cb&lv&#b392ca&le&#b58fc9&ll",
            mutableListOf("&7Unbreakable", "&#5a6bc6D&#5867c1e&#5763bcs&#555fb7t&#535bb2r&#5257aeu&#5052a9c&#4e4ea4t&#4c4a9fi&#4b469av&#494295e"),
            mutableListOf("&#2d273c\"&#322b43M&#373049a&#3c3450y &#413857t&#463d5dh&#4b4164e &#50456bm&#554a72o&#5a4e78o&#5f527fn &#645686k&#695b8cn&#6e5f93o&#73639aw &#7868a0a&#7d6ca7l&#806fabl &#8371afy&#8674b3o&#8977b8u&#8c79bcr &#8f7cc0s&#927fc4e&#9682c8c&#9984ccr&#9c87d0e&#9f8ad4t&#a28cd9s&#a58fdd.&#a892e1.&#ab94e5.&#ae97e9\"","","§fBreaks blocks in a 3x3 radius"),
            Material.NETHERITE_SHOVEL,
            mutableListOf("darkmoonshovel","cuboid"),
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.SILK_TOUCH to 1)
        )
        item.unbreakable = true
        return Pair("darkmoonshovel", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val blockBreakEvent: BlockBreakEvent? = event as? BlockBreakEvent

        when (type) {
            Action.BREAK_BLOCK -> {
                //AbilityUtil.breakThreeByThree(blockBreakEvent!!.block, player, BestTool.SHOVEL)
            }
            else -> return false
        }
        return true
    }
}