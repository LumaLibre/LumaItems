package dev.lumas.lumaitems.items.weapons.scythe

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.Tier
import java.util.function.Consumer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

@Disable(WorldName.PINATA)
class DazzlingHeartsScytheItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("dazzling-hearts-scythe")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#f848b6:#f848b6>Dazz</gradient><gradient:#f848b6:#ffa5e3>ling </gradient><gradient:#ffa5e3:#ffdef8>Hear</gradient><gradient:#ffdef8:#ddb8ff>ts Sc</gradient><gradient:#ddb8ff:#ab8df7>ythe</gradient></b>")
            .customEnchants("<#ffa5e3>Dazzling Barrage")
            .material(Material.NETHERITE_HOE)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2025)
            .lore(
                "<#ddb8ff>Right-click<white> to unleash a",
                "barrage of slashes.",
                "",
                "<red>Cooldown<gray>:<red> 8s"
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 9,
                Enchantment.BANE_OF_ARTHROPODS to 7,
                Enchantment.UNBREAKING to 10,
                Enchantment.LOOTING to 6,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId) || !player.isItemInSlot(KEY, EquipmentSlot.HAND)) {
            return
        }


        val loc = (player.getTargetEntity(50) as? LivingEntity)?.location
            ?: player.getTargetBlockExact(10)?.location
            ?: player.location.add(player.location.direction.multiply(10))

        // cooldown
        QuickTasks.addCooldown(this, player.uniqueId, 160L)
        dazzlingSolstice(player, loc)
    }

    private fun dazzlingSolstice(p: Player, loc: Location) {
        val entities: MutableList<LivingEntity> = mutableListOf()
        loc.getWorld().getNearbyEntities(loc, 2.0, 2.5, 2.0).forEach(Consumer { e: Entity ->
            if (e is LivingEntity && e != p) {
                entities.add(e)
            }
        })
        entities.forEach(Consumer { livingEntity: LivingEntity ->
            livingEntity.damage(14.0, p)
            livingEntity.velocity = Vector(0, 0, 0)
            livingEntity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 56, 3, false, false, false))
        })

        // runnables
        // i want to clean this up but ehhh
        val slashes = loc.syncTimer(0, 1) { task ->
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.23f)
            loc.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                loc.clone().add(0.0, random().nextDouble(2.0), 0.0),
                1,
                random().nextDouble(1.5),
                random().nextDouble(0.1),
                random().nextDouble(1.5)
            )
        }
        for (i in 0..5) {
            loc.syncDelayed((7 * (i + 1)).toLong()) {
                loc.getWorld().getNearbyEntities(loc, 2.0, 2.5, 2.0)
                    .forEach(Consumer { e: Entity ->
                        if (e is LivingEntity && e != p) {
                            entities.add(e)
                        }
                    })
                entities.forEach(Consumer { livingEntity: LivingEntity ->
                    livingEntity.damage(14.0, p)
                })
                if (i == 5) slashes.cancel()
            }
        }
    }
}