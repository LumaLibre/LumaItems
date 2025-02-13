package dev.jsinco.luma.lumaitems.items

import com.iridium.iridiumcolorapi.IridiumColorAPI
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.enums.RomanNumeral
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
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

    var tier: String = "&#AC87FB&lAstral", //"&#ffc8c8&lC&#ffcfc8&le&#ffd5c7&ll&#ffdcc7&le&#ffe3c7&ls&#ffe9c6&lt&#fff0c6&li&#fff6c5&la&#fffdc5&ll"

    var unbreakable: Boolean = false,
    var hideEnchants: Boolean = false,
    var addSpace: Boolean = true,
    var autoHat: Boolean = false,
    var attributeModifiers: MutableMap<Attribute, AttributeModifier> = mutableMapOf(),
    val stringPersistentDatas: MutableMap<NamespacedKey, String> = mutableMapOf(),
    var quotes: MutableList<String> = mutableListOf(),
    var b64PHead: String? = null,
    var spoofEnchants: Boolean = false,
    var persistentDataValue: Short = 1
) {

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
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

    val item = ItemStack(material)
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

    fun createItem(): ItemStack {
        if (meta == null) return item

        meta.persistentDataContainer.set(NamespacedKey(plugin, "lumaitem"), PersistentDataType.SHORT, 1)
        for (name in persistentData) {
            meta.persistentDataContainer.set(NamespacedKey(plugin, name), PersistentDataType.SHORT, persistentDataValue)
        }

        for (stringPersistentData in stringPersistentDatas) {
            meta.persistentDataContainer.set(stringPersistentData.key, PersistentDataType.STRING, stringPersistentData.value)
        }


        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ARMOR_TRIM, ItemFlag.HIDE_UNBREAKABLE, ItemFlag.HIDE_DYE)

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
            combinedLore.addAll(lore.map { "<white>$it" })
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
            meta.persistentDataContainer.set(NamespacedKey(plugin, "autohat"), PersistentDataType.SHORT, 1)
        }

        if (b64PHead != null && material == Material.PLAYER_HEAD) {
            Util.setBase64Texture(meta, b64PHead)
        }

        item.itemMeta = meta
        return item
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
        private var stringPersistentDatas: MutableMap<NamespacedKey, String> = mutableMapOf()
        private var quotes: MutableList<String> = mutableListOf()
        private var b64PHead: String? = null
        private var spoofEnchants: Boolean = false
        private var persistentDataValue: Short = 1

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
        fun stringPersistentDatas(stringPersistentDatas: MutableMap<NamespacedKey, String>) = apply { this.stringPersistentDatas = stringPersistentDatas }
        fun stringPersistentData(key: String, value: String) = apply { this.stringPersistentDatas[NamespacedKey(plugin, key)] = value }
        fun quotes(quotes: MutableList<String>) = apply { this.quotes = quotes }
        @SafeVarargs
        fun quotes(vararg quotes: String) = apply { this.quotes = quotes.toMutableList() }
        fun b64PHead(b64PHead: String) = apply { this.b64PHead = b64PHead }
        fun spoofEnchants(spoofEnchants: Boolean) = apply { this.spoofEnchants = spoofEnchants }
        fun persistentDataValue(persistentDataValue: Short) = apply { this.persistentDataValue = persistentDataValue }

        fun build() = ItemFactory(
            name, customEnchants, lore, material, persistentData, vanillaEnchants,
            tier, unbreakable, hideEnchants, addSpace, autoHat, attributeModifiers, stringPersistentDatas, quotes, b64PHead,
            spoofEnchants, persistentDataValue
        ).apply { miniMessage() }

        fun buildNoMiniMessage() = ItemFactory(
            name, customEnchants, lore, material, persistentData, vanillaEnchants,
            tier, unbreakable, hideEnchants, addSpace, autoHat, attributeModifiers, stringPersistentDatas, quotes, b64PHead,
            spoofEnchants, persistentDataValue
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