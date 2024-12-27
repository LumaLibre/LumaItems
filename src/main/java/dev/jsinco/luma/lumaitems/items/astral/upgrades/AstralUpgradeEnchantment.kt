package dev.jsinco.luma.lumaitems.items.astral.upgrades

import dev.jsinco.luma.lumaitems.enums.GenericMCToolType
import org.bukkit.enchantments.Enchantment

data class AstralUpgradeEnchantment (
    val enchantment: Enchantment,
    val level: Int,
    val applyTo: List<GenericMCToolType>?
) {
    companion object {

        fun deserializeAndRemoveApplyTo(string: String): Pair<List<GenericMCToolType>?, String> {
            if (!string.contains("-apply:")) return Pair(null, string)

            val split = string.split("-apply:")

            val tools = split[1].split(",")
            val toolsToApplyTo: MutableList<GenericMCToolType> = mutableListOf()
            for (tool in tools) {
                toolsToApplyTo.add(GenericMCToolType.valueOf(tool.uppercase()))
            }

            val newString = split[0].replace(" ", "").trim()

            return Pair(toolsToApplyTo, newString)
        }

        fun deserializeToolsToApplyTo(string: String?): List<GenericMCToolType>? {
            if (string == null) return null
            if (!string.contains("-apply:")) return null

            val tools = string.split("-apply:")[1].split(",")
            val toolsToApplyTo: MutableList<GenericMCToolType> = mutableListOf()
            for (tool in tools) {
                toolsToApplyTo.add(GenericMCToolType.valueOf(tool))
            }
            return toolsToApplyTo
        }

        fun serializeToolsToApplyTo(tools: List<GenericMCToolType>?): String? {
            if (tools == null) return null
            val toolsToApplyTo: MutableList<String> = mutableListOf()
            for (tool in tools) {
                toolsToApplyTo.add(tool.name)
            }
            return "-apply:${toolsToApplyTo.joinToString(",")}"
        }
    }
}