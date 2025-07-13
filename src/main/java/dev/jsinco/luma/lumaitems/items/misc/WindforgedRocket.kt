package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.FireworkMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin

class WindforgedRocket : CustomItemFunctions() {

    companion object {
        private const val CUSTOM_ITEM_KEY = "windforged-rocket"
    }

    private val plugin = JavaPlugin.getProvidingPlugin(this::class.java)
    private val key = NamespacedKey(plugin, CUSTOM_ITEM_KEY)

    private val infiniteFireworkRocket: ItemStack = ItemFactory.builder()
        .name("<b><gradient:#E90000:#E90000>Wind</gradient><gradient:#E90000:#FFFFFF>forged Roc</gradient><gradient:#FFFFFF:#FFFFFF>ket</gradient></b>")
        .customEnchants("<#D42424>Boundless Boost")
        .lore(
            "Forged from the breath of Zephyr,",
            "eternal wind of the western skies."
        )
        .material(Material.FIREWORK_ROCKET)
        .vanillaEnchants(Enchantment.UNBREAKING to 10)
        .tier(Tier.SUMMER_2025)
        .persistentData(CUSTOM_ITEM_KEY)
        .build()
        .createItem().apply {
            val meta = this.itemMeta as FireworkMeta
            meta.power = 3
            meta.persistentDataContainer.set(key, PersistentDataType.BYTE, 1)
            this.itemMeta = meta
        }

    override fun createItem(): Pair<String, ItemStack> {
        return Pair(CUSTOM_ITEM_KEY, infiniteFireworkRocket)
    }
}