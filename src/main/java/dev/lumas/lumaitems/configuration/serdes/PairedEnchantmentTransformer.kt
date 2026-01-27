package dev.lumas.lumaitems.configuration.serdes

import dev.lumas.lumaitems.configuration.model.PairedEnchantment
import dev.lumas.lumaitems.enums.ToolType
import eu.okaeri.configs.schema.GenericsPair
import eu.okaeri.configs.serdes.BidirectionalTransformer
import eu.okaeri.configs.serdes.SerdesContext
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey

class PairedEnchantmentTransformer : BidirectionalTransformer<String, PairedEnchantment>() {
    override fun getPair(): GenericsPair<String, PairedEnchantment> {
        return this.genericsPair(String::class.java, PairedEnchantment::class.java)
    }

    override fun leftToRight(data: String, serdesContext: SerdesContext): PairedEnchantment {
        val parts = data.split("/")
        if (parts.size != 2) {
            throw IllegalArgumentException("Invalid PairedEnchantment format: $data")
        }
        val enchantmentString = parts[0]
        val levelString = parts[1]
        val enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(enchantmentString))
            ?: throw IllegalArgumentException("Enchantment with key $enchantmentString not found")
        val level = levelString.toIntOrNull() ?: throw IllegalArgumentException("Invalid level: $levelString")

        if (data.contains("-apply:", true)) {
            val toolTypes: MutableList<ToolType> = mutableListOf()
            val applyPart = data.substringAfter("-apply:").trim()
            val applyTypes = applyPart.split(",")
            for (type in applyTypes) {
                val toolType = ToolType.entries.find { it.name.equals(type.trim(), ignoreCase = true) }
                if (toolType != null) {
                    toolTypes.add(toolType)
                } else {
                    throw IllegalArgumentException("Invalid ToolType: $type")
                }
            }
            return PairedEnchantment(enchantment, level, toolTypes)
        }

        return PairedEnchantment(enchantment, level)
    }

    override fun rightToLeft(data: PairedEnchantment, serdesContext: SerdesContext): String {
        if (data.apply.isEmpty()) {
            return "${data.enchantment.key.key}/${data.level}"
        } else {
            val applyTypes = data.apply.joinToString(",") { it.name.lowercase() }
            return "${data.enchantment.key.key}/${data.level} -apply:$applyTypes"
        }
    }
}