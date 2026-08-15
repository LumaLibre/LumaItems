package dev.lumas.lumaitems.items.weapons.incursion

import dev.lumas.lumaitems.util.extensions.sync
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
    override val displayName = "<b><aqua>Cryo Egg</aqua></b>"
    override val material = Material.BLUE_EGG
    override val burstColor: Color = Color.AQUA

    override val loreLines = listOf(
        "<gray>Throw it. It goes off where it lands.",
        "<gray>Frosts & slows whoever it catches."
    )

    override fun applyEffect(target: LivingEntity) {
        target.sync {
            if (!target.isValid || target.isDead) return@sync

            target.freezeTicks = max(target.freezeTicks, FREEZE_TICKS)
            target.addPotionEffect(
                PotionEffect(PotionEffectType.SLOWNESS, SLOWNESS_TICKS, SLOWNESS_AMPLIFIER, false, true, true)
            )
        }
    }
}
