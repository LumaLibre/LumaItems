package dev.jsinco.luma.lumaitems.items

import com.iridium.iridiumcolorapi.IridiumColorAPI
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.enums.RomanNumeral
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.PaperDataComponent
import dev.jsinco.luma.lumaitems.util.PersistentDataRecord
import dev.jsinco.luma.lumaitems.util.UnValuedPaperDataComponent
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.ValuedPaperDataComponent
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import io.papermc.paper.datacomponent.DataComponentType.Valued
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

// Breakdown:
// - This class is for creating custom items easily.
// - Our constructor takes in the parameters that generally most custom items always have and builds an item based off of them.
// - Some other options are available outside the constructor.

class ItemFactory(
    private val name: String,
    private val customEnchants: MutableList<String>,
    private val lore: MutableList<String>,
    private val material: Material,
    private val persistentData: MutableList<String>,
    private val vanillaEnchants: MutableMap<Enchantment, Int>,

    var tier: String = "&#AC87FB&lAstral",

    var unbreakable: Boolean = false,
    var hideEnchants: Boolean = false,
    var addSpace: Boolean = true,
    var autoHat: Boolean = false,
    /**
     * Attribute modifiers.
     */
    var attributeModifiers: MutableMap<Attribute, AttributeModifier> = mutableMapOf(),
    /**
     * Quotes.
     */
    var quotes: MutableList<String> = mutableListOf(),
    /**
     * Base 64 head if this item's material is a PLAYER_HEAD
     */
    var b64PHead: String? = null,
    /**
     * If true, vanilla enchants will be hidden and fake enchants will be written using
     * lore.
     */
    var spoofEnchants: Boolean = false,
    /**
     * The value of {@link ItemFactory#persistentData} in the item meta.
     */
    var persistentDataValue: Short = 1,
    /**
     * Persistent data records.
     */
    var persistentDataRecords: MutableList<PersistentDataRecord<*, *>> = mutableListOf(),

    var paperDataComponents: MutableList<PaperDataComponent> = mutableListOf(),
    var amount: Int = 1,
) {

    companion object {
        val AUTO_HAT_KEY = Util.namespacedKey("autohat")
        private val plugin: LumaItems = LumaItems.getInstance()
        @JvmField
        val LUMAITEM = Util.namespacedKey("lumaitem")
        private val tierFormat = listOf(
            "",
            "&#EEE1D5&m       &r&#EEE1D5⋆⁺₊⋆ ★ ⋆⁺₊⋆&m       ",
            "&#EEE1D5Tier • <PLACEHOLDER>",
            "&#EEE1D5&m       &r&#EEE1D5⋆⁺₊⋆ ★ ⋆⁺₊⋆&m       "
        )
        private val miniMessageTierFormat = listOf(
            "",
            "<#EEE1D5><st>       </st>⋆⁺₊⋆ ★ ⋆⁺₊⋆<st>       </st></#EEE1D5>",
            "<#EEE1D5>Tier •</#EEE1D5> <PLACEHOLDER>",
            "<#EEE1D5><st>       </st>⋆⁺₊⋆ ★ ⋆⁺₊⋆<st>       </st></#EEE1D5>"
        )
        @JvmStatic
        fun builder() = Builder()
    }

    val item = ItemStack(material, amount)
    val meta: Damageable? = item.itemMeta as? Damageable
    var miniMessage = false
    var hideDefaultAttributes = true

    fun miniMessage(): ItemFactory {
        miniMessage = true
        return this
    }

    fun addQuote(s: String): ItemFactory {
        quotes.add(s)
        return this
    }

    fun addGradientQuote(s: String, color1: String, color2: String): ItemFactory {
        val strippedColor1 = color1.replace("#", "").replace("&", "").trim()
        val strippedColor2 = color2.replace("#", "").replace("&", "").trim()
        quotes.add(IridiumColorAPI.process("<GRADIENT:$strippedColor1>\"$s\"</GRADIENT:$strippedColor2>"))
        return this
    }

    fun <P, C : Any> addPersistentData(key: NamespacedKey, type: PersistentDataType<P, C>, value: C): ItemFactory {
        if (meta == null) return this
        meta.persistentDataContainer.set(key, type, value)
        return this
    }

    fun <T : Any> addDataComponents(type: Valued<T>, value: T): ItemFactory {
        item.setData(type, value)
        return this
    }

    fun addAttributeContainer(attributeContainer: AttributeContainer): ItemFactory {
        this.attributeModifiers[attributeContainer.attribute] = AttributeModifier(attributeContainer.key, attributeContainer.amount, attributeContainer.operation, attributeContainer.slot)
        return this
    }

    fun <P, C : Any> setPersistentData(
        container: PersistentDataContainer,
        data: PersistentDataRecord<P, C>
    ) {
        container.set(data.nameSpacedKey, data.persistentDataType, data.value)
    }


    fun <T : Any> addValuedDataComponent(paperDataComponent: ValuedPaperDataComponent<T>) {
        item.setData(paperDataComponent.dataComponentType, paperDataComponent.value)
    }

    fun createItem(): ItemStack {
        if (meta == null) return item

        meta.persistentDataContainer.set(LUMAITEM, PersistentDataType.SHORT, 1)
        for (name in persistentData) {
            meta.persistentDataContainer.set(NamespacedKey(plugin, name), PersistentDataType.SHORT, persistentDataValue)
        }


        for (genericPersistentData in persistentDataRecords) {
            setPersistentData(meta.persistentDataContainer, genericPersistentData)
        }


        meta.addItemFlags(
            ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ARMOR_TRIM,
            ItemFlag.HIDE_UNBREAKABLE,
            ItemFlag.HIDE_DYE,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP
        )

        val combinedLore: MutableList<String> = mutableListOf()

        if (spoofEnchants) {
            val preAppend = if (!miniMessage) "&7" else "<gray>"
            for (enchant in vanillaEnchants) {
                val postAppend = if (enchant.value > 1) " ${RomanNumeral.fromInt(enchant.value)}" else ""
                combinedLore.add("$preAppend${Util.formatEnchantKey(enchant.key.key.toString())}$postAppend")
            }
            hideEnchants = true
        }

        combinedLore.addAll(customEnchants)


        if ((addSpace && lore.isNotEmpty()) || quotes.isNotEmpty()) combinedLore.add("")

        if (quotes.isNotEmpty()) {
            combinedLore.addAll(quotes)
            combinedLore.add("")
        }


        if (!miniMessage) {
            combinedLore.addAll(lore.map { "&f$it" })
            combinedLore.addAll(tierFormat.map { it.replace("<PLACEHOLDER>", tier) })

            meta.setDisplayName(Util.colorcode(name))
            meta.lore = Util.colorcodeList(combinedLore)
        } else {
            combinedLore.addAll(lore.map { it })
            combinedLore.addAll(miniMessageTierFormat.map { it.replace("<PLACEHOLDER>", tier) })

            meta.displayName(MiniMessageUtil.mm(name).decorationIfAbsent(TextDecoration.BOLD, TextDecoration.State.TRUE))
            meta.lore(MiniMessageUtil.mml(combinedLore).map { it.colorIfAbsent(NamedTextColor.WHITE) })
        }

        for (enchant in vanillaEnchants) {
            meta.addEnchant(enchant.key, enchant.value, true)
        }
        if (attributeModifiers.isNotEmpty()) {
            for (attributeModifier in attributeModifiers) {
                meta.addAttributeModifier(attributeModifier.key, attributeModifier.value)
            }
        } else if (hideDefaultAttributes) {
            val defaultAttributes = DefaultAttributes.getFromMaterial(material)
            if (defaultAttributes != null) {
                for (attributeModifier in defaultAttributes.attributes) {
                    meta.addAttributeModifier(attributeModifier.key, attributeModifier.value)
                }
            }
        }
        meta.isUnbreakable = unbreakable
        if (hideEnchants) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS)

        if (autoHat) {
            meta.persistentDataContainer.set(AUTO_HAT_KEY, PersistentDataType.SHORT, 1)
        }

        if (b64PHead != null && material == Material.PLAYER_HEAD) {
            Util.setBase64Texture(meta, b64PHead)
        }

        item.itemMeta = meta

        for (paperDataComponent in paperDataComponents) {
            if (paperDataComponent is ValuedPaperDataComponent<*>) {
                addValuedDataComponent(paperDataComponent)
            } else if (paperDataComponent is UnValuedPaperDataComponent) {
                item.setData(paperDataComponent.dataComponentType)
            }
        }
        return item
    }

    fun toReturnablePair(): Pair<String, ItemStack> {
        return this.toReturnablePair(0)
    }

    fun toReturnablePair(keyIndex: Int): Pair<String, ItemStack> {
        val key = persistentData.getOrNull(keyIndex) ?: run {
            LumaItems.log("Error: persistentData must have exactly one value!")
            return Pair("", ItemStack(Material.AIR))
        }
        return Pair(key, this.createItem())
    }


    class Builder {
        private var name: String = ""
        private var customEnchants: MutableList<String> = mutableListOf()
        private var lore: MutableList<String> = mutableListOf()
        private var material: Material = Material.AIR
        private var persistentData: MutableList<String> = mutableListOf()
        private var vanillaEnchants: MutableMap<Enchantment, Int> = mutableMapOf()
        private var tier: String = Tier.ASTRAL.toString()
        private var unbreakable: Boolean = false
        private var hideEnchants: Boolean = false
        private var addSpace: Boolean = true
        private var autoHat: Boolean = false
        private var attributeModifiers: MutableMap<Attribute, AttributeModifier> = mutableMapOf()
        private var quotes: MutableList<String> = mutableListOf()
        private var b64PHead: String? = null
        private var spoofEnchants: Boolean = false
        private var persistentDataValue: Short = 1
        private var persistentDataRecords: MutableList<PersistentDataRecord<*, *>> = mutableListOf()
        private var paperDataComponents: MutableList<PaperDataComponent> = mutableListOf()
        private var amount: Int = 1

        @SafeVarargs
        fun name(name: String) = apply { this.name = name }
        fun customEnchants(customEnchants: MutableList<String>) = apply { this.customEnchants = customEnchants }
        @SafeVarargs
        fun customEnchants(vararg customEnchants: String) = apply { this.customEnchants = customEnchants.toMutableList() }
        fun lore(lore: MutableList<String>) = apply { this.lore = lore }
        @SafeVarargs
        fun lore(vararg lore: String) = apply { this.lore = lore.toMutableList() }
        fun material(material: Material) = apply { this.material = material }
        fun material(s: String) = apply { this.material = Material.matchMaterial(s) ?: Material.AIR }
        fun persistentData(persistentData: MutableList<String>) = apply { this.persistentData = persistentData }
        @SafeVarargs
        fun persistentData(vararg persistentData: String) = apply { this.persistentData = persistentData.toMutableList() }
        @SafeVarargs
        fun persistentData(vararg persistentData: NamespacedKey) = apply { this.persistentData = persistentData.map { it.key }.toMutableList() }
        @SafeVarargs
        fun persistentDataRecords(vararg persistentData: PersistentDataRecord<*, *>) = apply { this.persistentDataRecords = persistentData.toMutableList() }
        @SafeVarargs
        fun paperDataComponents(vararg paperDataComponents: PaperDataComponent) = apply { this.paperDataComponents = paperDataComponents.toMutableList() }
        fun vanillaEnchants(vanillaEnchants: MutableMap<Enchantment, Int>) = apply { this.vanillaEnchants = vanillaEnchants }
        @SafeVarargs
        fun vanillaEnchants(vararg vanillaEnchants: Pair<Enchantment, Int>) = apply { this.vanillaEnchants = vanillaEnchants.toMap().toMutableMap() }
        fun tier(tier: String) = apply { this.tier = tier }
        fun tier (tier: Tier) = apply { this.tier = tier.tierString }
        fun unbreakable(unbreakable: Boolean) = apply { this.unbreakable = unbreakable }
        fun hideEnchants(hideEnchants: Boolean) = apply { this.hideEnchants = hideEnchants }
        fun addSpace(addSpace: Boolean) = apply { this.addSpace = addSpace }
        fun autoHat(autoHat: Boolean) = apply { this.autoHat = autoHat }
        fun attributeModifiers(attributeModifiers: MutableMap<Attribute, AttributeModifier>) = apply { this.attributeModifiers = attributeModifiers }
        @Suppress("UnstableApiUsage")
        fun attributeModifiers(vararg containers: AttributeContainer) = apply {
            this.attributeModifiers = containers.associate {
                it.attribute to AttributeModifier(it.key, it.amount, it.operation, it.slot)
            }.toMutableMap()
        }
        fun quotes(quotes: MutableList<String>) = apply { this.quotes = quotes }
        @SafeVarargs
        fun quotes(vararg quotes: String) = apply { this.quotes = quotes.toMutableList() }
        fun b64PHead(b64PHead: String) = apply { this.b64PHead = b64PHead }
        fun spoofEnchants(spoofEnchants: Boolean) = apply { this.spoofEnchants = spoofEnchants }
        fun persistentDataValue(persistentDataValue: Short) = apply { this.persistentDataValue = persistentDataValue }
        fun amount(amount: Int) = apply { this.amount = amount }

        fun build() = ItemFactory(
            name, customEnchants, lore, material, persistentData, vanillaEnchants,
            tier, unbreakable, hideEnchants, addSpace, autoHat, attributeModifiers, quotes, b64PHead,
            spoofEnchants, persistentDataValue, persistentDataRecords, paperDataComponents, amount
        ).apply { miniMessage() }

        fun buildNoMiniMessage() = ItemFactory(
            name, customEnchants, lore, material, persistentData, vanillaEnchants,
            tier, unbreakable, hideEnchants, addSpace, autoHat, attributeModifiers, quotes, b64PHead,
            spoofEnchants, persistentDataValue, persistentDataRecords, paperDataComponents, amount
        )

        fun buildPair(): Pair<String, ItemStack> {
            if (persistentData.size != 1) {
                LumaItems.log("Error: persistentData must have exactly one value!")
                return Pair("", ItemStack(Material.AIR))
            }
            return Pair(persistentData[0], build().createItem())
        }

    }
}