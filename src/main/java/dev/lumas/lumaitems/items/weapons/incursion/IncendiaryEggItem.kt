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
    override val displayName = "<b><gradient:#e08a6a:#e6a05f:#e9b673>Incendiary Egg</gradient></b>"
    override val customEnchant = "<#EDB172>Scorch"
    override val material = Material.BROWN_EGG
    override val burstColor: Color = Color.ORANGE

    override val loreLines = listOf(
        "Best before: several",
        "weeks ago, arguably.",
        "",
        "Do not preheat! It has",
        "handled that already.",
        "",
        "<red>Cooldown: 1.5s"
    )

    override fun applyEffect(target: LivingEntity) {
        target.sync {
            if (!target.isValid || target.isDead) return@sync
            target.fireTicks = max(target.fireTicks, FIRE_TICKS)
        }
    }
}
