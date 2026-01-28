package dev.lumas.lumaitems.configuration.serdes

import eu.okaeri.configs.schema.GenericsPair
import eu.okaeri.configs.serdes.BidirectionalTransformer
import eu.okaeri.configs.serdes.SerdesContext
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

class EnchantmentTransformer : BidirectionalTransformer<String, Enchantment>() {
    override fun getPair(): GenericsPair<String, Enchantment> {
        return this.genericsPair(String::class.java, Enchantment::class.java)
    }

    override fun leftToRight(data: String, serdesContext: SerdesContext): Enchantment {
        val enchantment = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT).get(NamespacedKey.minecraft(data))
        return enchantment ?: throw IllegalArgumentException("Enchantment with key $data not found")
    }

    override fun rightToLeft(data: Enchantment, serdesContext: SerdesContext): String {
        return data.key.key
    }
}