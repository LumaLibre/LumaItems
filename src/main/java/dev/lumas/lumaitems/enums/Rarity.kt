package dev.lumas.lumaitems.enums

import dev.lumas.lumaitems.configuration.files.RelicsYml
import dev.lumas.lumaitems.registry.Registry
import org.bukkit.Material

enum class Rarity(val rgb: String, val algorithmWeight: Int, val friendlyName: String) {
    ASTRAL("#AC87FB", 14, "Astral"),
    LUNAR("#6255fb", 7, "Lunar"),
    NOVA("#75c3fb", 2, "Nova"),
    PULSAR("#c773fb", 1, "Pulsar"),
    SOLAR("#EEFB5F", 1, "Solar"),
    DELTA("#DE509D", 1, "Delta");


    val tier: String = "<" + this.rgb + ">" + "<b>" + this.friendlyName


    val materials: List<Material>
        get() {
            val relicMaterialsSection =
                Registry.CONFIGS.getOrThrow(RelicsYml::class.java).relicMaterials

            return when (this) {
                ASTRAL -> emptyList()
                LUNAR -> relicMaterialsSection.lunar
                NOVA -> relicMaterialsSection.nova
                PULSAR -> relicMaterialsSection.pulsar
                SOLAR -> relicMaterialsSection.solar
                DELTA -> relicMaterialsSection.delta
            }
        }

    companion object {
        // Data
        val BOSS = listOf(NOVA)
        val GENERIC = listOf(PULSAR, SOLAR, DELTA)
    }
}
