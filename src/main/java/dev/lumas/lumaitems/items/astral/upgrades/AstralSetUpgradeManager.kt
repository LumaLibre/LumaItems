package dev.lumas.lumaitems.items.astral.upgrades

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.manager.FileManager
import dev.lumas.lumaitems.enums.ToolType
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment

open class AstralSetUpgradeManager {
    companion object {
        val modifiableMaterials: List<ToolType> = listOf(
            ToolType.HELMET, ToolType.CHESTPLATE, ToolType.LEGGINGS, ToolType.BOOTS,
            ToolType.SWORD, ToolType.PICKAXE, ToolType.AXE, ToolType.SHOVEL, ToolType.HOE
        )

        @JvmStatic val upgrades: MutableMap<String, MutableList<AstralUpgradeTier>> = mutableMapOf()
        val plugin: LumaItems = LumaItems.getInstance()
    }

    init {
        //reloadUpgrades()
    }

    fun reloadUpgrades() {
        val fileManager = FileManager("astral.yml").generateYamlFile()

        for (setKey in fileManager.getConfigurationSection("astral-upgrades")?.getKeys(false) ?: throw NullPointerException("Section does not exist")) {
            val setTierSection = fileManager.getConfigurationSection("astral-upgrades.$setKey") ?: continue
            val setTierSectionList = setTierSection.getKeys(false)
            for (setTier in setTierSectionList) {
                val astralMaterial = AstralMaterial.valueOf(setTierSection.getString("$setTier.material") ?: "IRON")
                val enchantsListString = setTierSection.getStringList("$setTier.enchants")
                val setTierNumber = setTier.replace("tier-", "").trim().toInt()

                val astralUpgradeTier = AstralUpgradeTier(
                    setKey,
                    astralMaterial,
                    getEnchantsFromStringList(enchantsListString),
                    setTierNumber,
                    setTierSectionList.last() == setTier
                )

                if (upgrades.contains(setKey)) {
                    upgrades[setKey]?.add(astralUpgradeTier)
                } else {
                    upgrades[setKey] = mutableListOf(astralUpgradeTier)
                }
            }
        }
    }

    private fun getEnchantsFromStringList(stringEnchants: MutableList<String>): List<AstralUpgradeEnchantment> {
        val enchants: MutableList<AstralUpgradeEnchantment> = mutableListOf()
        for (stringEnchant in stringEnchants) {
            val pair = AstralUpgradeEnchantment.deserializeAndRemoveApplyTo(stringEnchant)

            val toolsToApplyTo: List<ToolType>? = pair.first
            val finalStringEnchant = pair.second

            val split = finalStringEnchant.split("/")
            if (split.size != 2) {
                plugin.logger.severe("Invalid enchantment! $finalStringEnchant")
                continue
            }

            val enchantment: Enchantment? = Enchantment.getByKey(NamespacedKey.minecraft(split[0].lowercase()))
            if (enchantment == null) {
                plugin.logger.severe("Invalid enchantment! ${split[0]}")
                continue
            }
            enchants.add(AstralUpgradeEnchantment(enchantment, split[1].toInt(), toolsToApplyTo))
        }
        return enchants
    }
}