package dev.lumas.lumaitems.configuration.model

import dev.lumas.lumaitems.enums.ToolType
import org.bukkit.enchantments.Enchantment

class PairedEnchantment(
    var enchantment: Enchantment,
    var level: Int,
    var apply: List<ToolType> = listOf()
) {

    constructor(enchantment: Enchantment, level: Int, vararg apply: ToolType) : this(enchantment, level, apply.toList())

}