package dev.lumas.lumaitems.items.misc

import com.destroystokyo.paper.event.entity.EntityAddToWorldEvent
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.annotations.FireAnyways
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.syncDelayed
import io.papermc.paper.event.entity.EntityEquipmentChangedEvent
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.AbstractNautilus
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

@FireAnyways(Action.ENTITY_EQUIPMENT_CHANGED, Action.ENTITY_ADD_TO_WORLD)
class TidewardenBardingItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("tidewarden-barding")
        private val ARMED = ConcurrentHashMap.newKeySet<UUID>()

        private const val TRAIL_PERIOD_TICKS = 3L
        private const val TRAIL_TRAIL_DISTANCE = 0.8
        private val TRAIL_DUST = Particle.DustTransition(
            Color.fromRGB(0x1E, 0x8A, 0xBF),
            Color.fromRGB(0x9B, 0xE4, 0xDF),
            1.1f
        )

        private fun isWearingBarding(nautilus: AbstractNautilus): Boolean {
            return nautilus.equipment.getItem(EquipmentSlot.BODY).isMatchingItem(KEY)
        }

        private fun refresh(nautilus: AbstractNautilus, celebrate: Boolean) {
            if (!nautilus.isValid) return

            if (isWearingBarding(nautilus)) {
                if (apply(nautilus) && celebrate) {
                    burst(nautilus)
                }
            } else {
                revert(nautilus)
            }
        }

        private fun apply(nautilus: AbstractNautilus): Boolean {
            nautilus.isInvulnerable = true
            nautilus.isPersistent = true
            nautilus.removeWhenFarAway = false

            val id = nautilus.uniqueId
            if (!ARMED.add(id)) return false

            val task = nautilus.scheduler.runAtFixedRate(
                LumaItems.getInstance(),
                { task -> trail(nautilus, task) },
                { ARMED.remove(id) },
                1L,
                TRAIL_PERIOD_TICKS
            )

            if (task == null) {
                ARMED.remove(id)
                return false
            }
            return true
        }

        private fun revert(nautilus: AbstractNautilus) {
            if (!ARMED.remove(nautilus.uniqueId)) return
            nautilus.isInvulnerable = false
            nautilus.removeWhenFarAway = !nautilus.isTamed
        }

        private fun trail(nautilus: AbstractNautilus, task: ScheduledTask) {
            if (!nautilus.isValid) {
                ARMED.remove(nautilus.uniqueId)
                task.cancel()
                return
            }

            if (!isWearingBarding(nautilus)) {
                task.cancel()
                revert(nautilus)
                return
            }

            if (nautilus.trackedBy.isEmpty()) return

            val wake = nautilus.location
                .subtract(nautilus.location.direction.multiply(TRAIL_TRAIL_DISTANCE))
                .add(0.0, 0.35, 0.0)

            nautilus.world.spawnParticle(Particle.DUST_COLOR_TRANSITION, wake, 2, 0.18, 0.14, 0.18, 0.0, TRAIL_DUST)
            nautilus.world.spawnParticle(Particle.NAUTILUS, wake, 1, 0.1, 0.1, 0.1, 0.0)
            nautilus.world.spawnParticle(Particle.GLOW, wake, 1, 0.14, 0.12, 0.14, 0.0)
        }

        private fun burst(nautilus: AbstractNautilus) {
            val at = nautilus.location.add(0.0, 0.5, 0.0)

            nautilus.world.spawnParticle(Particle.NAUTILUS, at, 30, 0.5, 0.4, 0.5, 0.05)
            nautilus.world.spawnParticle(Particle.DUST_COLOR_TRANSITION, at, 24, 0.5, 0.4, 0.5, 0.0, TRAIL_DUST)
            nautilus.world.playSound(at, Sound.BLOCK_CONDUIT_ACTIVATE, 0.7f, 1.4f)
        }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#1e8abf:#9be4df:#f8898a>Tidewarden Barding</gradient></b>")
            .vanillaEnchants(Enchantment.AQUA_AFFINITY to 10)
            .customEnchants("<#9be4df>Unsinkable")
            .material(Material.DIAMOND_NAUTILUS_ARMOR)
            .persistentData(KEY)
            .unbreakable(true)
            .tier(Tier.LUMARINE_2026)
            .lore(
                "Fit this to a <#9be4df>nautilus</#9be4df>",
                "to bind it to the tide.",
                "",
                "It will take no harm, and",
                "it will not wander off",
                "into the deep."
            )
            .buildPair()
    }

    override fun onEntityEquipmentChanged(event: EntityEquipmentChangedEvent) {
        val nautilus = event.entity as? AbstractNautilus ?: return
        event.equipmentChanges[EquipmentSlot.BODY] ?: return
        refresh(nautilus, celebrate = true)
    }

    override fun onEntityAddToWorld(event: EntityAddToWorldEvent) {
        val nautilus = event.entity as? AbstractNautilus ?: return
        refresh(nautilus, celebrate = false)
    }

    override fun onPlayerInteractEntity(player: Player, event: PlayerInteractEntityEvent) {
        val nautilus = event.rightClicked as? AbstractNautilus ?: return
        nautilus.syncDelayed(1) {
            refresh(nautilus, celebrate = true)
        }
    }
}
