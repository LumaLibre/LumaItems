package dev.lumas.lumaitems.configuration.files

import dev.lumas.lumaitems.configuration.File
import dev.lumas.lumaitems.configuration.OkaeriFile
import dev.lumas.lumaitems.configuration.model.AstralSetClass
import dev.lumas.lumaitems.configuration.model.PairedEnchantment
import dev.lumas.lumaitems.items.astral.sets.MagmaticSet
import dev.lumas.lumaitems.items.astral.upgrades.AstralMaterial
import eu.okaeri.configs.OkaeriConfig
import eu.okaeri.configs.annotation.Comment
import eu.okaeri.configs.annotation.CustomKey
import kotlin.reflect.KClass
import org.bukkit.enchantments.Enchantment

@File("astral.yml")
class AstralYml : OkaeriFile() {

    @Comment("Class name and then chance of item")
    @CustomKey("astral-orb-rarities")
    val astralOrbRarities: Map<String, Int> = mapOf(
        "MistralSet" to 35,
        "ArchaelSet" to 38,
        "VenomSet" to 43,
        "MagmaticSet" to 0,
        "FalterSet" to 3,
        "MelukaSet" to 42,
        "ValleySet" to 70,
        "BlitzSet" to 10,
        "ReforgedSet" to 20,
        "KazkanSet" to 11,
        "HarbingerSet" to 8,
        "VerdantSet" to 25
    )

    val astralUpgrades: Map<AstralSetClass<*>, AstralUpgradeTier> = buildMap {
        this += AstralUpgradeTier.builder()
            .setAstralSetClass(MagmaticSet::class)
            .setTier(2)
            .setMaterial(AstralMaterial.NETHERITE)
            .setEnchants(
                PairedEnchantment.of(Enchantment.MENDING, 1),
                PairedEnchantment.of(Enchantment.FIRE_ASPECT, 4),
                PairedEnchantment.of(Enchantment.EFFICIENCY, 6),
                PairedEnchantment.of(Enchantment.LOOTING, 4),
                PairedEnchantment.of(Enchantment.FORTUNE, 4),
                PairedEnchantment.of(Enchantment.UNBREAKING, 8),
                PairedEnchantment.of(Enchantment.FEATHER_FALLING, 5)
            )
            .buildPair()


    }




    data class AstralUpgradeTier(
        val tier: Int,
        val material: AstralMaterial,
        val enchants: List<PairedEnchantment>
    ) : OkaeriConfig() {

        companion object {
            fun builder(): Builder {
                return Builder()
            }
        }

        class Builder {
            var astralSetClass : AstralSetClass<*>? = null
            var tier: Int = 1
            var material: AstralMaterial = AstralMaterial.DIAMOND
            var enchants: List<PairedEnchantment> = listOf()

            fun setAstralSetClass(astralSetClass: AstralSetClass<*>): Builder {
                this.astralSetClass = astralSetClass
                return this
            }

            fun setAstralSetClass(setClass: Class<*>): Builder {
                this.astralSetClass = AstralSetClass.of(setClass)
                return this
            }

            fun setAstralSetClass(setClass: KClass<*>): Builder {
                this.astralSetClass = AstralSetClass.of(setClass.java)
                return this
            }

            fun setTier(tier: Int): Builder {
                this.tier = tier
                return this
            }

            fun setMaterial(material: AstralMaterial): Builder {
                this.material = material
                return this
            }

            fun setEnchants(enchants: List<PairedEnchantment>): Builder {
                this.enchants = enchants
                return this
            }

            fun setEnchants(vararg enchants: PairedEnchantment): Builder {
                this.enchants = enchants.toList()
                return this
            }

            fun buildPair(): Pair<AstralSetClass<*>, AstralUpgradeTier> {
                val setClass = astralSetClass ?: throw IllegalStateException("AstralSetClass must be provided")
                return setClass to AstralUpgradeTier(tier, material, enchants)
            }
        }
    }
}