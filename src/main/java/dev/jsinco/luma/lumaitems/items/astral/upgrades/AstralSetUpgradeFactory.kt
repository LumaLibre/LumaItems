package dev.jsinco.luma.lumaitems.items.astral.upgrades

import dev.jsinco.luma.lumaitems.enums.GenericMCToolType
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.ChatColor
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

        val currentLore = meta.lore ?: return
        for ((i, loreLine) in currentLore.withIndex()) {
            val loreLineStripped = ChatColor.stripColor(loreLine) ?: continue
            if (loreLineStripped.contains("Tier • Astral")) {
                currentLore[i] = Util.colorcode("&#EEE1D5Tier • &#AC87FB&lAstral&#CAB5F6+")
                break
            }
        }

        meta.lore = currentLore
        item.itemMeta = meta
    }

    companion object {
        fun upgradeAstralItem(item: ItemStack, upgradeTier: AstralUpgradeTier) {
            val genericMCToolType = GenericMCToolType.getToolType(item)

            if (modifiableMaterials.contains(genericMCToolType)) {
                // TODO: Look more into exactly why this is deprecated. Haven't experienced any of the issues mentioned
                val originalGearType = item.type.toString().split("_")[1]
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

            meta.persistentDataContainer.set(NamespacedKey(plugin, upgradeTier.tierName), PersistentDataType.SHORT, upgradeTier.tierNumber.toShort())
            item.itemMeta = meta
        }
    }
}