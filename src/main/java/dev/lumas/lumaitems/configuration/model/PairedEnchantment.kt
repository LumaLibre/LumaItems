package dev.lumas.lumaitems.configuration.model

import dev.lumas.lumaitems.enums.ToolType
import org.bukkit.enchantments.Enchantment

class PairedEnchantment(
    val enchantment: Enchantment,
    val level: Int,
    val apply: List<ToolType> = listOf()
) {
    companion object {
        fun of(enchantment: Enchantment, level: Int, apply: List<ToolType> = listOf()): PairedEnchantment {
            return PairedEnchantment(enchantment, level, apply)
        }
    }
}