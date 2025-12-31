package dev.lumas.lumaitems.items.astral.upgrades

import dev.lumas.lumaitems.enums.DefaultAttributes
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.util.MiniMessageUtil
import dev.lumas.lumaitems.util.tiers.Tier
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

// Notes:
// Upgrade tier names should be the set's identifiers (e.g. mistral-set) and should have a short value of the actual tier.

class AstralSetUpgradeFactory (val item: ItemStack) : AstralSetUpgradeManager() {

    fun upgrade(): Boolean {
        val upgradeTier: AstralUpgradeTier = determineUpgradeTier() ?: return false
        upgradeAstralItem(item, upgradeTier)
        if (upgradeTier.maxTier) {
            updateAstralItemTier(item)
        }
        return true
    }
    

    private fun determineUpgradeTier(): AstralUpgradeTier? {
        val dataContainer = item.itemMeta?.persistentDataContainer ?: return null
        for (upgrade in upgrades) {
            val tierNumber = dataContainer.get(NamespacedKey(plugin, upgrade.key), PersistentDataType.SHORT) ?: continue
            for (upgradeTier: AstralUpgradeTier in upgrade.value) {
                if (upgradeTier.tierNumber == tierNumber.toInt() + 1) {
                    return upgradeTier
                }
            }
        }
        return null
    }

    private fun updateAstralItemTier(item: ItemStack) {
        val meta = item.itemMeta ?: return

        val currentLore = meta.lore() ?: return
        for ((i, loreLine) in currentLore.withIndex()) {
            val loreLineStripped = PlainTextComponentSerializer.plainText().serialize(loreLine)
            if (loreLineStripped.contains("Tier •")) {
                currentLore[i] = MiniMessageUtil.mm("<#EEE1D5>Tier • ${Tier.ASTRAL}<#CAB5F6>+")
                break
            }
        }

        meta.lore(currentLore)
        item.itemMeta = meta
    }

    companion object {
        fun upgradeAstralItem(item: ItemStack, upgradeTier: AstralUpgradeTier) {
            val genericMCToolType = ToolType.getToolType(item)

            if (modifiableMaterials.contains(genericMCToolType)) {
                val originalGearType = item.type.toString().split("_")[1]
                // FIXME: Deprecated method usage
                item.type = Material.valueOf("${upgradeTier.newMaterial}_${originalGearType}")
            }

            val meta = item.itemMeta ?: return

            for (astralUpgradeEnchant in upgradeTier.newEnchantments) {
                val enchantment = astralUpgradeEnchant.enchantment
                if (astralUpgradeEnchant.applyTo != null && astralUpgradeEnchant.applyTo.contains(genericMCToolType)) {
                    meta.addEnchant(enchantment, astralUpgradeEnchant.level, true)
                } else if (enchantment.canEnchantItem(item)) {
                    meta.addEnchant(enchantment, astralUpgradeEnchant.level, true)
                }
            }

            // bad patch for an even worse system, but it works for now
            DefaultAttributes.getFromMaterial(item.type)?.attributes?.let { newAttributes ->
                for (entry in newAttributes) {
                    val attribute = entry.key
                    val value = entry.value
                    meta.removeAttributeModifier(attribute)
                    meta.addAttributeModifier(attribute, value)
                }
            }

            meta.persistentDataContainer.set(NamespacedKey(plugin, upgradeTier.tierName), PersistentDataType.SHORT, upgradeTier.tierNumber.toShort())
            item.itemMeta = meta
        }
    }
}

//    private fun updateAstralItemTier(item: ItemStack) {
//        val meta = item.itemMeta ?: return
//
//        val currentLore = meta.lore ?: return
//        for ((i, loreLine) in currentLore.withIndex()) {
//            val loreLineStripped = ChatColor.stripColor(loreLine) ?: continue
//            if (loreLineStripped.contains("Tier • Astral")) {
//                currentLore[i] = Util.colorcode("&#EEE1D5Tier • &#AC87FB&lAstral&#CAB5F6+")
//                break
//            }
//        }
//
//        meta.lore = currentLore
//        item.itemMeta = meta
//    }