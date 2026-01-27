package dev.lumas.lumaitems.items.astral.upgrades

import dev.lumas.lumaitems.configuration.files.AstralYml

class WrappedAstralUpgradeTier(
    val setId: String,
    val upgradeTier: AstralYml.OkaeriAstralUpgradeTier,
    val maxTier: Boolean
) {

    val tier = upgradeTier.tier
    val material = upgradeTier.material
    val enchants = upgradeTier.enchants
}