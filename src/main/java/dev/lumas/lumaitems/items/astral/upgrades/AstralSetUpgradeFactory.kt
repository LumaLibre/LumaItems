package dev.lumas.lumaitems.items.astral.upgrades

import dev.lumas.lumaitems.configuration.files.AstralYml
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.Tier
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

// Notes:
// Upgrade tier names should be the set's identifiers (e.g. mistral-set) and should have a short value of the actual tier.

class AstralSetUpgradeFactory (val item: ItemStack) {


    fun upgrade(): Boolean {
        val upgradeTier: WrappedAstralUpgradeTier = determineUpgradeTier() ?: return false
        upgradeAstralItem(item, upgradeTier)
        if (upgradeTier.maxTier) {
            updateAstralItemTier(item)
        }
        return true
    }
    

    private fun determineUpgradeTier(): WrappedAstralUpgradeTier? {
        val dataContainer = item.itemMeta?.persistentDataContainer ?: return null
        val upgradeTiers = Registry.CONFIGS.getOrThrow(AstralYml::class).astralUpgrades
        for (entry in upgradeTiers) {
            val setId = entry.key
            val upgrades = entry.value
            val currentTier = dataContainer.get(Util.namespacedKey(setId), PersistentDataType.SHORT) ?: continue

            for (upgradeTier: AstralYml.OkaeriAstralUpgradeTier in upgrades) {
                val canUpgrade = upgradeTier.tier == currentTier.toInt() + 1
                if (canUpgrade) {
                    val isMaxTier = upgradeTier.tier >= upgrades.maxOf { it.tier }
                    return WrappedAstralUpgradeTier(setId, upgradeTier, isMaxTier)
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
                currentLore[i] = "<#EEE1D5>Tier • ${Tier.ASTRAL}<#CAB5F6>+".asComponent()
                break
            }
        }

        meta.lore(currentLore)
        item.itemMeta = meta
    }

    companion object {
        val MODIFIABLE_MATERIALS: List<ToolType> = listOf(
            ToolType.HELMET, ToolType.CHESTPLATE, ToolType.LEGGINGS, ToolType.BOOTS,
            ToolType.SWORD, ToolType.PICKAXE, ToolType.AXE, ToolType.SHOVEL, ToolType.HOE
        )

        fun upgradeAstralItem(item: ItemStack, upgradeTier: WrappedAstralUpgradeTier) {
            val genericMCToolType = ToolType.getToolType(item)

            // TODO: Needs better logic
            if (MODIFIABLE_MATERIALS.contains(genericMCToolType)) {
                val originalGearType = item.type.toString().split("_")[1]
                // FIXME: Deprecated method usage
                item.type = Material.valueOf("${upgradeTier.material}_${originalGearType}")
            }

            val meta = item.itemMeta ?: return

            for (astralUpgradeEnchant in upgradeTier.enchants) {
                val enchantment = astralUpgradeEnchant.enchantment
                if (astralUpgradeEnchant.apply.isNotEmpty() && astralUpgradeEnchant.apply.contains(genericMCToolType)) {
                    meta.addEnchant(enchantment, astralUpgradeEnchant.level, true)
                } else if (enchantment.canEnchantItem(item)) {
                    meta.addEnchant(enchantment, astralUpgradeEnchant.level, true)
                }
            }

            // bad patch for an even worse system, but it works for now
            item.type.defaultAttributeModifiers.entries().let { newAttributes ->
                for (entry in newAttributes) {
                    val attribute = entry.key
                    val value = entry.value
                    meta.removeAttributeModifier(attribute)
                    meta.addAttributeModifier(attribute, value)
                }
            }

            meta.persistentDataContainer.set(Util.namespacedKey(upgradeTier.setId), PersistentDataType.SHORT, upgradeTier.tier.toShort())
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