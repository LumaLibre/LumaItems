package dev.lumas.lumaitems.items.weapons.incursion

import dev.lumas.lumaitems.util.extensions.sync
import kotlin.math.max
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.entity.LivingEntity

class IncendiaryEggItem : IncursionEggItem() {

    companion object {
        private const val FIRE_TICKS = 100
    }

    override val key = "incursion-incendiary-egg"
    override val displayName = "<b><gold>Incendiary Egg</gold></b>"
    override val material = Material.BROWN_EGG
    override val burstColor: Color = Color.ORANGE

    override val loreLines = listOf(
        "<gray>Throw it. It goes off where it lands.",
        "<gray>Sets whoever it catches alight."
    )

    override fun applyEffect(target: LivingEntity) {
        target.sync {
            if (!target.isValid || target.isDead) return@sync
            target.fireTicks = max(target.fireTicks, FIRE_TICKS)
        }
    }
}
