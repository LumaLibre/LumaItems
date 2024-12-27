package dev.jsinco.luma.lumaitems.items.astral.upgrades

data class AstralUpgradeTier(
    val tierName: String,
    val newMaterial: AstralMaterial,
    val newEnchantments: List<AstralUpgradeEnchantment>,
    val tierNumber: Int,
    val maxTier: Boolean
) {
    override fun toString(): String {
        return "AstralUpgradeTier(tierName='$tierName', newMaterial=$newMaterial, newEnchantments=$newEnchantments, tierNumber=$tierNumber, maxTier=$maxTier)"
    }
}