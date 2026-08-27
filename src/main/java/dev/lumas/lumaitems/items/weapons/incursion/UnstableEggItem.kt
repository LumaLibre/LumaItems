package dev.lumas.lumaitems.items.weapons.incursion

import org.bukkit.Color
import org.bukkit.Material

class UnstableEggItem : IncursionEggItem() {

    override val key = "incursion-unstable-egg"
    override val displayName = "<b><gradient:#cdd6de:#d6d2c8:#c9d9d2>Unstable Egg</gradient></b>"
    override val customEnchant = "<#cfd6dd>Volatile"
    override val material = Material.EGG
    override val burstColor: Color = Color.WHITE
    override val damage = 80.0

    override val loreLines = listOf(
        "Best before: several",
        "weeks ago, arguably.",
        "",
        "Press your <#cfd6dd>swap key</#cfd6dd>",
        "<#cfd6dd>(F)</#cfd6dd> to cycle variants.",
        "",
        "<red>Cooldown: 3.5s"
    )
}
