package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.syncTimer
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack

class CottonTailCrossbowItem : CustomItem {

    companion object {
        private val dustColors: Set<Color> = setOf(
            Util.hex2BukkitColor("#EC62B6"),
            Util.hex2BukkitColor("#FDADAD")
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#EC62B6&lC&#ED66B5&lo&#EE6BB5&lt&#EF6FB4&lt&#F074B4&lo&#F178B3&ln&#F27CB3&lt&#F381B2&la&#F485B2&li&#F58AB1&ll &#F68EB1&lC&#F793B0&lr&#F897B0&lo&#F99BAF&ls&#FAA0AF&ls&#FBA4AE&lb&#FCA9AE&lo&#FDADAD&lw",
            mutableListOf("&#EC62B6Veloci-Bunny"),
            mutableListOf("Arrows fired by this weapon", "will travel faster and deal", "more damage"),
            Material.CROSSBOW,
            mutableListOf("cottontailcrossbow"),
            mutableMapOf(Enchantment.PIERCING to 7, Enchantment.QUICK_CHARGE to 4, Enchantment.UNBREAKING to 9, Enchantment.MENDING to 1)
        )
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("cottontailcrossbow", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.PROJECTILE_LAUNCH -> {
                event as ProjectileLaunchEvent

                val arrow: Arrow = event.entity as Arrow

                arrow.isPersistent = false
                arrow.setGravity(false)
                arrow.velocity = arrow.velocity.multiply(1.8)
                arrow.isCritical = false

                arrow.syncTimer(0, 1) {
                    arrow.world.spawnParticle(Particle.DUST, arrow.location, 2, 0.1, 0.1, 0.1, 0.1,
                        Particle.DustOptions(dustColors.random(), 1f))
                    arrow.world.spawnParticle(Particle.FLAME, arrow.location, 1, 0.1, 0.1, 0.1, 0.0)

                    if (arrow.isDead || arrow.ticksLived >= 200) {
                        it.cancel()
                    }
                }
            }
            else -> return false
        }
        return true
    }
}