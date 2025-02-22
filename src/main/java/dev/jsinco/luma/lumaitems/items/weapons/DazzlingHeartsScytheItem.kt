package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.function.Consumer

@Disable(WorldName.SPAWN)
class DazzlingHeartsScytheItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#f848b6:#f848b6>Dazz</gradient><gradient:#f848b6:#ffa5e3>ling </gradient><gradient:#ffa5e3:#ffdef8>Hear</gradient><gradient:#ffdef8:#ddb8ff>ts Sc</gradient><gradient:#ddb8ff:#ab8df7>ythe</gradient></b>")
            .customEnchants("<#ffa5e3>Dazzling Barrage")
            .material(Material.NETHERITE_HOE)
            .persistentData("dazzling-hearts-scythe")
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
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) return
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
            if (e is LivingEntity && !AbilityUtil.noDamagePermission(p, e) && e != p) {
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
        val slashes = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance(), {
            loc.getWorld().playSound(loc, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1.23f)
            loc.getWorld().spawnParticle(
                Particle.SWEEP_ATTACK,
                loc.clone().add(0.0, random().nextDouble(2.0), 0.0),
                1,
                random().nextDouble(1.5),
                random().nextDouble(0.1),
                random().nextDouble(1.5)
            )
        }, 0, 1)
        for (i in 0..5) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(instance(), {
                loc.getWorld().getNearbyEntities(loc, 2.0, 2.5, 2.0)
                    .forEach(Consumer { e: Entity ->
                        if (e is LivingEntity && AbilityUtil.noDamagePermission(p, e) && e != p) {
                            entities.add(e)
                        }
                    })
                entities.forEach(Consumer { livingEntity: LivingEntity ->
                    livingEntity.damage(14.0, p)
                })
                if (i == 5) Bukkit.getScheduler().cancelTask(slashes)
            }, (7 * (i + 1)).toLong())
        }
    }
}