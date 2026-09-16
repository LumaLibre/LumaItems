package dev.lumas.lumaitems.items.misc;

import dev.lumas.lumaitems.model.item.ItemFactory;
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.Tier;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.GameMode
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.math.ceil
import kotlin.math.hypot

class SuperchargedBreezeRod : CustomItemFunctions() {

    companion object {
        private const val KEY = "supercharged-breeze-rod"
        private const val KNOCKBACK_POWER = 10.0
        private const val MAX_HORIZONTAL_STEP = 3.5
        private const val BASE_AIR_DRAG = 0.91
        private const val DEFAULT_BLOCK_FRICTION = 0.6
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#1AC9CC:#5d85dc:#CA51CB>Supercharged Breeze Rod</gradient></b>")
            .customEnchants(
                "<gray>Knockback VIII", // intentional - custom knockback is applied
                "<gradient:#1AC9CC:#5d85dc>Gust Strike</gradient>"
            )
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .lore(
                "Stolen from a Breeze that",
                "was struck down by /smite.",
                "",
                "<#3CA7D4>Strike</#3CA7D4> enemies to send them",
                "soaring with a burst of wind."
            )
            .material(Material.BREEZE_ROD)
            .persistentData(KEY)
            .tier(Tier.STAFF)
            .buildPair()
    }

    override fun onPrepareCraft(player: Player, event: PrepareItemCraftEvent) {
        event.inventory.result = null
    }

    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        if (!player.isItemInSlot(KEY, EquipmentSlot.HAND)) return
        val entity = event.entity as? LivingEntity ?: return
        if (entity.isRealPlayer()) return
        gustStrike(player, entity)
    }

    override fun onPlayerPreAttackEntity(player: Player, event: PrePlayerAttackEntityEvent) {
        if (!event.willAttack() || !player.isItemInSlot(KEY, EquipmentSlot.HAND)) return
        val target = event.attacked as? Player ?: return
        if (!target.isRealPlayer() || target.gameMode == GameMode.SPECTATOR) return
        gustStrike(player, target)
    }

    private fun LivingEntity.isRealPlayer() = this is Player && !this.hasMetadata("NPC")

    private fun gustStrike(player: Player, entity: LivingEntity) {
        val launch = player.location.direction.multiply(KNOCKBACK_POWER)

        if (entity.isRealPlayer()) {
            entity.syncDelayed(1) { _ -> entity.velocity = launch }
        } else {
            launchInSteps(entity, launch)
        }

        val loc = entity.location.add(0.0, 0.5, 0.0)
        entity.world.playSound(loc, Sound.ENTITY_WIND_CHARGE_WIND_BURST, 0.9f, 0.9f)
        entity.world.playSound(loc, Sound.ENTITY_PLAYER_ATTACK_KNOCKBACK, 1.0f, 1.1f)
        entity.world.spawnParticle(Particle.GUST_EMITTER_SMALL, loc, 1, 0.05, 0.05, 0.05, 0.01)
        entity.world.spawnParticle(Particle.DUST_PLUME, loc, 25, 0.3, 0.5, 0.3, 0.15)
    }

    // Minecraft calculates collisions for server-side entities one axis at a time, so large velocities can
    // hit blocks that are nowhere near the flight path of the entity. At some point Mojang has to fix this
    private fun launchInSteps(entity: LivingEntity, launch: Vector) {
        var tick = 0
        var steps = 1
        var step = launch

        entity.syncTimer(1, 1) { task ->
            if (!entity.isValid) {
                task.cancel()
                return@syncTimer
            }

            if (tick == 0) {
                val speed = hypot(launch.x, launch.z)
                if (speed > MAX_HORIZONTAL_STEP) {
                    val airDragModifier = entity.getAttribute(Attribute.AIR_DRAG_MODIFIER)?.value ?: 1.0
                    val airDrag = (1.0 - (1.0 - BASE_AIR_DRAG) * airDragModifier).coerceIn(0.0, 0.99)
                    val firstTickDrag = if (entity.isOnGround) {
                        val frictionModifier = entity.getAttribute(Attribute.FRICTION_MODIFIER)?.value ?: 1.0
                        (1.0 - (1.0 - DEFAULT_BLOCK_FRICTION) * frictionModifier).coerceIn(0.0, 1.0) * airDrag
                    } else {
                        airDrag
                    }

                    val decayTicks = 1.0 / (1.0 - airDrag) // sum of the geometric series f^0 + f^1 + ...
                    val distance = speed * (1.0 + firstTickDrag * decayTicks)
                    steps = (ceil(distance / MAX_HORIZONTAL_STEP - decayTicks).toInt() + 1).coerceAtLeast(2)

                    step = launch.clone().multiply(distance / (steps - 1 + decayTicks) / speed)
                }
            }

            entity.velocity = step
            if (++tick >= steps) task.cancel()
        }
    }
}
