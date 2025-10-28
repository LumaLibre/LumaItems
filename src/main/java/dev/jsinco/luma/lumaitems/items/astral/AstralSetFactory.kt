package dev.jsinco.luma.lumaitems.items.astral

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.Rarity
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.obj.PersistentDataRecord
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class AstralSetFactory (val name: String, val customEnchantNames: List<String>?) {

    constructor(name: String) : this(name, null)

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
    }

    var commonLore: List<String> = listOf()
    var identifier = "${name.lowercase()}-set"
    val createdAstralItems: MutableList<ItemStack> = mutableListOf()
    var commonEnchants: MutableMap<Enchantment, Int> = mutableMapOf()

    constructor(name: String, customEnchantNames: List<String>?, commonLore: List<String>) : this(name, customEnchantNames) {
        this.commonLore = commonLore
    }

    fun astralSetItem(material: Material, vanillaEnchants: MutableMap<Enchantment, Int>, includeCommonEnchants: Boolean): ItemStack {
        return astralSetItem(material, vanillaEnchants, commonLore, includeCommonEnchants)
    }

    fun astralSetItem(material: Material, vanillaEnchants: MutableMap<Enchantment, Int>, lore: List<String>): ItemStack {
        return astralSetItem(material, vanillaEnchants, lore, true)
    }

    fun astralSetItem(material: Material, vanillaEnchants: MutableMap<Enchantment, Int>, lore: List<String>, includeCommonEnchants: Boolean): ItemStack {
        return astralSetItem(material, vanillaEnchants, lore, includeCommonEnchants, null, null, null)
    }

    fun astralSetItem(material: Material, vanillaEnchants: MutableMap<Enchantment, Int>, lore: List<String>, includeCommonEnchants: Boolean, attributeModifiers: MutableMap<Attribute, AttributeModifier>?): ItemStack {
        return astralSetItem(material, vanillaEnchants, lore, includeCommonEnchants, attributeModifiers, null, null)
    }

    fun astralSetItem(
        material: Material,
        vanillaEnchants: MutableMap<Enchantment, Int>,
        lore: List<String>,
        includeCommonEnchants: Boolean,
        attributeModifiers: MutableMap<Attribute, AttributeModifier>?,
        customName: String?,
        customEnchants: List<String>?): ItemStack {


        // override common enchants
        val finalVanillaEnchants = if (includeCommonEnchants) {
            commonEnchants.toMutableMap().also { it.putAll(vanillaEnchants) }
        } else {
            vanillaEnchants
        }

        val item = ItemFactory(
            customName ?: "&#AC87FB&l$name &f${Util.getGearType(material)}",
            customEnchants?.toMutableList() ?: (this.customEnchantNames?.toMutableList() ?: mutableListOf()),
            lore.toMutableList(),
            material,
            mutableListOf(identifier),
            finalVanillaEnchants
        )

        if (attributeModifiers != null) {
            item.attributeModifiers = attributeModifiers
        }


        item.persistentDataRecords.add(PersistentDataRecord.create("relic-rarity", PersistentDataType.STRING, Rarity.ASTRAL.name))
        return item.createItem().also { createdAstralItems.add(it) }
    }

    fun astralSetItemGenericEnchantOnly(material: Material): ItemStack {
        return astralSetItemGenericEnchantOnly(material, commonLore, null)
    }

    fun astralSetItemGenericEnchantOnly(material: Material, lore: List<String>): ItemStack {
        return astralSetItemGenericEnchantOnly(material, lore, null)
    }

    fun astralSetItemGenericEnchantOnly(
        material: Material,
        lore: List<String>,
        attributeModifiers: MutableMap<Attribute, AttributeModifier>?): ItemStack {

        val enchants: MutableMap<Enchantment, Int> = mutableMapOf()

        for (enchant in commonEnchants) {
            if (enchant.key.canEnchantItem(ItemStack(material))) {
                enchants[enchant.key] = enchant.value
            }
        }

        return astralSetItem(material, enchants, lore, false, attributeModifiers)
    }

}