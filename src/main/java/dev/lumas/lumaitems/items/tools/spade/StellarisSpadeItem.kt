package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class StellarisSpadeItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#6adbff&lS&#75d4fc&lt&#7fcdf8&le&#8ac6f5&ll&#95bff2&ll&#9fb8ef&la&#aab1eb&lr&#b5aae8&li&#bfa3e5&ls&#ca9ce1&l' &#d495de&lS&#df8edb&lp&#ea87d8&la&#f480d4&ld&#ff79d1&le",
            mutableListOf("&#86b6e3P&#9face1i&#b8a1e0ñ&#d095dca&#e686d5t&#fc78cfa"),
            mutableListOf("&#ff79d1\"&#fa7cd2B&#f67fd4o&#f182d5u&#ed85d7n&#e888d8d&#e48bd9l&#df8edbe&#db91dcs&#d694des &#d297dff&#cd9ae0o&#c99de2r&#c4a0e3t&#c0a3e5u&#bba6e6n&#b7a9e7e &#b2abe9w&#aeaeeaa&#a9b1ebi&#a5b4edt&#a0b7eei&#9cbaf0n&#97bdf1g &#93c0f2b&#8ec3f4e&#8ac6f5y&#85c9f7o&#81ccf8n&#7ccff9d&#78d2fb.&#73d5fc.&#6fd8fe.&#6adbff\"","","&fOccasional chance to receive a pinata","&freward from breaking a block"),
            Material.NETHERITE_SHOVEL,
            mutableListOf("stellarisspade", "pinata"),
            mutableMapOf(Enchantment.EFFICIENCY to 8, Enchantment.SILK_TOUCH to 1, Enchantment.UNBREAKING to 9, Enchantment.MENDING to 1)
        )
        return Pair("stellarisspade", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return true
    }
}