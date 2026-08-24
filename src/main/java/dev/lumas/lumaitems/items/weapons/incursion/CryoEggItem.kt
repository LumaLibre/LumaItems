package dev.lumas.lumaitems.items.weapons.incursion

import kotlin.math.max
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class CryoEggItem : IncursionEggItem() {

    companion object {
        private const val FREEZE_TICKS = 200
        private const val SLOWNESS_TICKS = 100
        private const val SLOWNESS_AMPLIFIER = 2
    }

    override val key = "incursion-cryo-egg"
    override val displayName = "<b><gradient:#8fd0ea:#9dd9e2:#aedcd0>Cryo Egg</gradient></b>"
    override val customEnchant = "<#9be4df>Cold Snap"
    override val material = Material.BLUE_EGG
    override val burstColor: Color = Color.AQUA
    override val damage = 65.0

    override val loreLines = listOf(
        "Best before: several",
        "weeks ago, arguably.",
        "",
        "Best served chilled, which",
        "it insists on arranging itself.",
        "",
        "Press your <#9be4df>swap key (F)</#9be4df>",
        "to cycle variants.",
        "",
        "<red>Cooldown: 3.5s"
    )

    override fun applyEffect(target: LivingEntity) {
        target.freezeTicks = max(target.freezeTicks, FREEZE_TICKS)
        target.addPotionEffect(
            PotionEffect(PotionEffectType.SLOWNESS, SLOWNESS_TICKS, SLOWNESS_AMPLIFIER, false, true, true)
        )
    }
}
