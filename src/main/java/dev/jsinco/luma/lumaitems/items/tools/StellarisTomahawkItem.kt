package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class StellarisTomahawkItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#6adbff&lS&#73d5fc&lt&#7ccffa&le&#84caf7&ll&#8dc4f4&ll&#96bef1&la&#9fb8ef&lr&#a7b3ec&li&#b0ade9&ls&#b9a7e7&l' &#c2a1e4&lT&#ca9ce1&lo&#d396df&lm&#dc90dc&la&#e58ad9&lh&#ed85d6&la&#f67fd4&lw&#ff79d1&lk",
            mutableListOf("&#86b6e3P&#9face1i&#b8a1e0ñ&#d095dca&#e686d5t&#fc78cfa"),
            mutableListOf("&#ff79d1\"&#fa7cd2B&#f67fd4o&#f182d5u&#ed85d7n&#e888d8d&#e48bd9l&#df8edbe&#db91dcs&#d694des &#d297dff&#cd9ae0o&#c99de2r&#c4a0e3t&#c0a3e5u&#bba6e6n&#b7a9e7e &#b2abe9w&#aeaeeaa&#a9b1ebi&#a5b4edt&#a0b7eei&#9cbaf0n&#97bdf1g &#93c0f2b&#8ec3f4e&#8ac6f5y&#85c9f7o&#81ccf8n&#7ccff9d&#78d2fb.&#73d5fc.&#6fd8fe.&#6adbff\"","","&fOccasional chance to receive a pinata","&freward from breaking a block"),
            Material.NETHERITE_AXE,
            mutableListOf("stellaristomahawk", "pinata"),
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.SILK_TOUCH to 1, Enchantment.UNBREAKING to 9, Enchantment.MENDING to 1)
        )
        return Pair("stellaristomahawk", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val blockBreakEvent: BlockBreakEvent? = event as? BlockBreakEvent
        when (type) {
            Action.BREAK_BLOCK -> {
                AbilityUtil.pinataAbility(blockBreakEvent!!.block)
            }

            else -> return false
        }
        return true
    }
}