package dev.lumas.lumaitems.configuration.files

import dev.lumas.lumaitems.annotations.File
import dev.lumas.lumaitems.configuration.OkaeriFile
import dev.lumas.lumaitems.configuration.model.AstralSetClass
import dev.lumas.lumaitems.configuration.model.PairedEnchantment
import dev.lumas.lumaitems.enums.ToolType
import dev.lumas.lumaitems.items.astral.sets.ArchaelSet
import dev.lumas.lumaitems.items.astral.sets.BlitzSet
import dev.lumas.lumaitems.items.astral.sets.FalterSet
import dev.lumas.lumaitems.items.astral.sets.HarbingerSet
import dev.lumas.lumaitems.items.astral.sets.KazkanSet
import dev.lumas.lumaitems.items.astral.sets.MagmaticSet
import dev.lumas.lumaitems.items.astral.sets.MelukaSet
import dev.lumas.lumaitems.items.astral.sets.ReforgedSet
import dev.lumas.lumaitems.items.astral.sets.ValleySet
import dev.lumas.lumaitems.items.astral.sets.VenomSet
import dev.lumas.lumaitems.items.astral.sets.VerdantSet
import dev.lumas.lumaitems.items.astral.upgrades.AstralMaterial
import dev.lumas.lumaitems.registry.Registry
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import org.bukkit.enchantments.Enchantment
import org.jetbrains.annotations.Range

@File("astral.yml")
class AstralYml : OkaeriFile() {

    @Comment("Class name and then chance of item")
    @CustomKey("astral-orb-rarities")
    var astralOrbRarities: Map<AstralSetClass, Int> = mapOf(
        AstralSetClass(MagmaticSet::class) to 35,
        AstralSetClass(ArchaelSet::class) to 38,
        AstralSetClass(VenomSet::class) to 43,
        AstralSetClass(MagmaticSet::class) to 0,
        AstralSetClass(FalterSet::class) to 3,
        AstralSetClass(MelukaSet::class) to 42,
        AstralSetClass(ValleySet::class) to 70,
        AstralSetClass(BlitzSet::class) to 10,
        AstralSetClass(ReforgedSet::class) to 20,
        AstralSetClass(KazkanSet::class) to 11,
        AstralSetClass(HarbingerSet::class) to 8,
        AstralSetClass(VerdantSet::class) to 25
    )

    @Comment("All sets by default will start from tier 1. Do not go below tier 2 here.")
    @CustomKey("astral-upgrades")
    var astralUpgrades: Map<String, List<OkaeriAstralUpgradeTier>> = buildMap {
        buildTiers("magmatic-set") {
            tier(2) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.MENDING, 1),
                    PairedEnchantment(Enchantment.FIRE_ASPECT, 3),
                    PairedEnchantment(Enchantment.EFFICIENCY, 5),
                    PairedEnchantment(Enchantment.LOOTING, 3),
                    PairedEnchantment(Enchantment.FORTUNE, 3),
                    PairedEnchantment(Enchantment.UNBREAKING, 6),
                    PairedEnchantment(Enchantment.FEATHER_FALLING, 4)
                )
            }
        }

        buildTiers("venom-set") {
            tier(2) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.MENDING, 1),
                    PairedEnchantment(Enchantment.PROTECTION, 6),
                    PairedEnchantment(Enchantment.SHARPNESS, 7),
                    PairedEnchantment(Enchantment.UNBREAKING, 6),
                    PairedEnchantment(Enchantment.THORNS, 5)
                )
            }
        }

        buildTiers("meluka-set") {
            tier(2) {
                material(AstralMaterial.IRON)
                enchants(
                    PairedEnchantment(Enchantment.EFFICIENCY, 5),
                    PairedEnchantment(Enchantment.UNBREAKING, 5),
                    PairedEnchantment(Enchantment.RESPIRATION, 4),
                    PairedEnchantment(Enchantment.DEPTH_STRIDER, 4),
                    PairedEnchantment(Enchantment.PROTECTION, 6)
                )
            }
            tier(3) {
                material(AstralMaterial.DIAMOND)
                enchants(
                    PairedEnchantment(Enchantment.EFFICIENCY, 6),
                    PairedEnchantment(Enchantment.MENDING, 1),
                    PairedEnchantment(Enchantment.UNBREAKING, 6),
                    PairedEnchantment(Enchantment.RESPIRATION, 5)
                )
            }
            tier(4) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.EFFICIENCY, 7),
                    PairedEnchantment(Enchantment.UNBREAKING, 7),
                    PairedEnchantment(Enchantment.RESPIRATION, 6),
                    PairedEnchantment(Enchantment.PROTECTION, 7)
                )
            }
        }

        buildTiers("archael-set") {
            tier(2) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.MENDING, 1),
                    PairedEnchantment(Enchantment.UNBREAKING, 6),
                    PairedEnchantment(Enchantment.PROJECTILE_PROTECTION, 6)
                )
            }
        }

        buildTiers("mistral-set") {
            tier(2) {
                material(AstralMaterial.DIAMOND)
                enchants(
                    PairedEnchantment(Enchantment.PROTECTION, 6),
                    PairedEnchantment(Enchantment.PROJECTILE_PROTECTION, 7),
                    PairedEnchantment(Enchantment.SHARPNESS, 7),
                    PairedEnchantment(Enchantment.EFFICIENCY, 7),
                    PairedEnchantment(Enchantment.MENDING, 1)
                )
            }
            tier(3) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.UNBREAKING, 8),
                    PairedEnchantment(Enchantment.EFFICIENCY, 8),
                    PairedEnchantment(Enchantment.LUCK_OF_THE_SEA, 5)
                )
            }
        }

        buildTiers("falter-set") {
            tier(2) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.EFFICIENCY, 10),
                    PairedEnchantment(Enchantment.UNBREAKING, 10)
                )
            }
        }

        buildTiers("reforged-set") {
            tier(2) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.PROTECTION, 8),
                    PairedEnchantment(Enchantment.SOUL_SPEED, 3),
                    PairedEnchantment(Enchantment.AQUA_AFFINITY, 1),
                    PairedEnchantment(Enchantment.UNBREAKING, 7)
                )
            }
        }

        buildTiers("valley-set") {
            tier(2) {
                material(AstralMaterial.DIAMOND)
                enchants(
                    PairedEnchantment(Enchantment.EFFICIENCY, 7, ToolType.AXE, ToolType.PICKAXE, ToolType.SHOVEL, ToolType.HOE),
                    PairedEnchantment(Enchantment.UNBREAKING, 7)
                )
            }
            tier(3) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.EFFICIENCY, 8, ToolType.AXE, ToolType.PICKAXE, ToolType.SHOVEL, ToolType.HOE),
                    PairedEnchantment(Enchantment.LURE, 5)
                )
            }
        }

        buildTiers("blitz-set") {
            tier(2) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.MENDING, 1),
                    PairedEnchantment(Enchantment.UNBREAKING, 7),
                    PairedEnchantment(Enchantment.EFFICIENCY, 6, ToolType.AXE),
                    PairedEnchantment(Enchantment.FORTUNE, 4, ToolType.AXE),
                    PairedEnchantment(Enchantment.FEATHER_FALLING, 5, ToolType.ELYTRA),
                    PairedEnchantment(Enchantment.BLAST_PROTECTION, 4, ToolType.ELYTRA)
                )
            }
        }

        buildTiers("kazkan-set") {
            tier(2) {
                material(AstralMaterial.DIAMOND)
                enchants(
                    PairedEnchantment(Enchantment.UNBREAKING, 8, ToolType.AXE, ToolType.CROSSBOW, ToolType.MAGICAL, ToolType.SHIELD),
                    PairedEnchantment(Enchantment.MENDING, 1, ToolType.AXE, ToolType.CROSSBOW, ToolType.MAGICAL, ToolType.SHIELD),
                    PairedEnchantment(Enchantment.FIRE_ASPECT, 3, ToolType.MAGICAL),
                    PairedEnchantment(Enchantment.SHARPNESS, 7, ToolType.AXE),
                    PairedEnchantment(Enchantment.LOOTING, 4, ToolType.AXE),
                    PairedEnchantment(Enchantment.BANE_OF_ARTHROPODS, 6, ToolType.AXE),
                    PairedEnchantment(Enchantment.QUICK_CHARGE, 3, ToolType.CROSSBOW),
                    PairedEnchantment(Enchantment.PIERCING, 5, ToolType.CROSSBOW)
                )
            }
            tier(3) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.UNBREAKING, 9),
                    PairedEnchantment(Enchantment.FIRE_ASPECT, 5, ToolType.MAGICAL),
                    PairedEnchantment(Enchantment.KNOCKBACK, 3, ToolType.MAGICAL),
                    PairedEnchantment(Enchantment.SHARPNESS, 8, ToolType.AXE),
                    PairedEnchantment(Enchantment.LOOTING, 5, ToolType.AXE),
                    PairedEnchantment(Enchantment.BANE_OF_ARTHROPODS, 7, ToolType.AXE),
                    PairedEnchantment(Enchantment.QUICK_CHARGE, 4, ToolType.CROSSBOW),
                    PairedEnchantment(Enchantment.PIERCING, 6, ToolType.CROSSBOW)
                )
            }
        }

        buildTiers("harbinger-set") {
            tier(2) {
                material(AstralMaterial.DIAMOND)
                enchants(
                    PairedEnchantment(Enchantment.MENDING, 1),
                    PairedEnchantment(Enchantment.UNBREAKING, 6),
                    PairedEnchantment(Enchantment.PROTECTION, 8),
                    PairedEnchantment(Enchantment.RESPIRATION, 2, ToolType.HELMET),
                    PairedEnchantment(Enchantment.AQUA_AFFINITY, 1, ToolType.HELMET),
                    PairedEnchantment(Enchantment.FEATHER_FALLING, 4, ToolType.BOOTS, ToolType.ELYTRA),
                    PairedEnchantment(Enchantment.SWIFT_SNEAK, 2, ToolType.LEGGINGS),
                    PairedEnchantment(Enchantment.SOUL_SPEED, 2, ToolType.BOOTS)
                )
            }
            tier(3) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.UNBREAKING, 8),
                    PairedEnchantment(Enchantment.PROTECTION, 10),
                    PairedEnchantment(Enchantment.RESPIRATION, 3, ToolType.HELMET),
                    PairedEnchantment(Enchantment.FEATHER_FALLING, 6, ToolType.BOOTS, ToolType.ELYTRA),
                    PairedEnchantment(Enchantment.SWIFT_SNEAK, 3, ToolType.LEGGINGS),
                    PairedEnchantment(Enchantment.SOUL_SPEED, 3, ToolType.BOOTS)
                )
            }
        }

        buildTiers("verdant-set") {
            tier(2) {
                material(AstralMaterial.NETHERITE)
                enchants(
                    PairedEnchantment(Enchantment.MENDING, 1),
                    PairedEnchantment(Enchantment.UNBREAKING, 7),
                    PairedEnchantment(Enchantment.EFFICIENCY, 6, ToolType.AXE, ToolType.PICKAXE, ToolType.HOE, ToolType.SHOVEL),
                    PairedEnchantment(Enchantment.FORTUNE, 3, ToolType.AXE, ToolType.PICKAXE, ToolType.HOE, ToolType.SHOVEL),
                    PairedEnchantment(Enchantment.PROTECTION, 5, ToolType.HELMET, ToolType.CHESTPLATE, ToolType.LEGGINGS, ToolType.BOOTS),
                    PairedEnchantment(Enchantment.FIRE_PROTECTION, 4, ToolType.HELMET, ToolType.CHESTPLATE, ToolType.LEGGINGS, ToolType.BOOTS),
                    PairedEnchantment(Enchantment.BLAST_PROTECTION, 4, ToolType.HELMET, ToolType.CHESTPLATE, ToolType.LEGGINGS, ToolType.BOOTS),
                    PairedEnchantment(Enchantment.RESPIRATION, 3, ToolType.HELMET),
                    PairedEnchantment(Enchantment.FEATHER_FALLING, 5, ToolType.BOOTS)
                )
            }
        }
    }


    inline fun MutableMap<String, List<OkaeriAstralUpgradeTier>>.buildTiers(key: String, block: MutableList<OkaeriAstralUpgradeTier>.() -> Unit) {
        this[key] = buildList(block)
    }


    inline fun MutableList<OkaeriAstralUpgradeTier>.tier(int: @Range(from = 1, to = Int.MAX_VALUE.toLong()) Int, block: OkaeriAstralUpgradeTier.Builder.() -> Unit) {
        add(OkaeriAstralUpgradeTier.Builder().tier(int).apply(block).build())
    }


    class OkaeriAstralUpgradeTier(
        var tier: @Range(from = 1, to = Int.MAX_VALUE.toLong()) Int,
        var material: AstralMaterial,
        var enchants: List<PairedEnchantment>
    ) : OkaeriConfig() {

        fun isMaxTier(): Boolean {
            val configUpgradeTiers = Registry.CONFIGS.getOrThrow(AstralYml::class).astralUpgrades
            return isMaxTier(configUpgradeTiers.values)
        }

        fun isMaxTier(upgradeTiers: Collection<List<OkaeriAstralUpgradeTier>>): Boolean {
            val tierListForThisTier = upgradeTiers.find { it.contains(this) } ?: throw IllegalArgumentException("Tier list for this tier not found $this")
            return this.tier >= tierListForThisTier.maxOf { it.tier }
        }

        class Builder {
            var tier: Int = 1
            var material: AstralMaterial = AstralMaterial.DIAMOND
            var enchants: List<PairedEnchantment> = listOf()

            fun tier(tier: Int): Builder {
                this.tier = tier
                return this
            }

            fun material(material: AstralMaterial): Builder {
                this.material = material
                return this
            }

            fun enchants(enchants: List<PairedEnchantment>): Builder {
                this.enchants = enchants
                return this
            }

            fun enchants(vararg enchants: PairedEnchantment): Builder {
                this.enchants = enchants.toList()
                return this
            }

            fun build(): OkaeriAstralUpgradeTier {
                return OkaeriAstralUpgradeTier(tier, material, enchants)
            }
        }
    }
}