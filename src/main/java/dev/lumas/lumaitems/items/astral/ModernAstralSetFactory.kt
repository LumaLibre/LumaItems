package dev.lumas.lumaitems.items.astral

import dev.lumas.lumaitems.enums.Rarity
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.obj.AttributeContainer
import dev.lumas.lumaitems.obj.PaperDataComponent
import dev.lumas.lumaitems.obj.PersistentDataRecord
import dev.lumas.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class ModernAstralSetFactory(val identifier: String) {

    constructor(caller: AstralSet) : this(caller.setIdentifier())

    companion object {
        private val ASTRAL_PDC = PersistentDataRecord.create("relic-rarity", PersistentDataType.STRING, Rarity.ASTRAL.name)
    }

    private var name: String? = null
    private val customEnchants: MutableList<String> = mutableListOf()
    private val lore: MutableList<String> = mutableListOf()
    private val commonVanillaEnchants: MutableMap<Enchantment, Int> = mutableMapOf()


    private val createdAstralItems: MutableList<ItemStack> = mutableListOf()

    fun setName(name: String) = apply {
        this.name = name
    }

    fun withCustomEnchants(vararg enchantmentNames: String) = apply {
        this.customEnchants.addAll(enchantmentNames)
    }

    fun withLore(vararg loreLines: String) = apply {
        this.lore.addAll(loreLines)
    }

    fun withCommonVanillaEnchants(vararg enchantments: Pair<Enchantment, Int>) = apply {
        this.commonVanillaEnchants.putAll(enchantments)
    }

    fun itemBuilder(): ItemBuilder {
        return ItemBuilder(this)
    }

    fun items(): List<ItemStack> {
        return createdAstralItems.toList()
    }

    class ItemBuilder(
        val parent: ModernAstralSetFactory
    ) {
        private var material: Material = Material.AIR
        private var vanillaEnchants: MutableMap<Enchantment, Int> = mutableMapOf()
        private var overrideLore: MutableList<String> = mutableListOf()
        private var includeCommonEnchants: Boolean = true
        private var attributeModifiers: MutableList<AttributeContainer> = mutableListOf()
        private var dataComponents: MutableList<PaperDataComponent> = mutableListOf()
        private var overrideName: String? = null
        private var overrideCustomEnchants: MutableList<String>? = null

        fun material(material: Material) = apply { this.material = material }
        fun vanillaEnchants(vararg vanillaEnchants: Pair<Enchantment, Int>) = apply { this.vanillaEnchants.putAll(vanillaEnchants) }
        fun lore(vararg lore: String) = apply { this.overrideLore.addAll(lore) }
        fun includeCommonEnchants(include: Boolean) = apply { this.includeCommonEnchants = include }
        fun attributeModifiers(attributeModifiers: Collection<AttributeContainer>) = apply { this.attributeModifiers.addAll(attributeModifiers) }
        fun dataComponents(dataComponents: Collection<PaperDataComponent>) = apply { this.dataComponents.addAll(dataComponents) }
        fun overrideName(name: String?) = apply { this.overrideName = name }
        fun overrideCustomEnchants(customEnchants: Collection<String>?) = apply { this.overrideCustomEnchants = customEnchants?.toMutableList() }


        fun add(): ItemStack {
            val factory = ItemFactory.builder()
                .name(overrideName ?: "<b><#AC87FB>${this.parent.name}</#AC87FB></b> <white>${Util.getGearType(material)}</white>")
                .customEnchants(overrideCustomEnchants ?: this.parent.customEnchants.map { "<#AC87FB>$it</#AC87FB>" }.toMutableList())
                .lore(overrideLore.ifEmpty { this.parent.lore })
                .material(material)
                .persistentData(this.parent.identifier)
                .vanillaEnchants(
                    if (includeCommonEnchants) {
                        this.parent.commonVanillaEnchants.toMutableMap().also { it.putAll(vanillaEnchants) }
                    } else {
                        vanillaEnchants
                    }
                )
                .attributeModifiers(*attributeModifiers.toTypedArray())
                .paperDataComponents(*dataComponents.toTypedArray())
                .persistentDataRecords(ASTRAL_PDC)

            return factory.build().createItem().also { parent.createdAstralItems.add(it) }
        }
    }

}