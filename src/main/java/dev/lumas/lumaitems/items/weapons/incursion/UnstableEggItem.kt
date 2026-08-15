package dev.lumas.lumaitems.items.weapons.incursion

import org.bukkit.Color
import org.bukkit.Material

class UnstableEggItem : IncursionEggItem() {

    override val key = "incursion-unstable-egg"
    override val displayName = "<b><white>Unstable Egg</white></b>"
    override val material = Material.EGG
    override val burstColor: Color = Color.WHITE

    override val loreLines = listOf(
        "<gray>Throw it. It goes off where it lands."
    )
}
