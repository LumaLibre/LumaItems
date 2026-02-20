package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.annotations.FireAnyways
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

abstract class HeartstringBowItemNest : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("cupids-longbow")
        private val DUST = DustOptions(Color.fromRGB(245, 166, 194), 1.0f)
    }

    private val projectileKey = Util.namespacedKey("cupids-longbow-projectile")

    protected val base = ItemFactory.builder()
        .persistentData(KEY)
        .tier(Tier.VALENTIDE_2026)
        .customEnchants("<gradient:#F6C1D1:#E8B7E8:#D7C2F2:#F4B6C2>Attraction</gradient>")
        .lore(
            "Love pulls gently,",
            "but surely.",
            "",
            "Hit entities are",
            "drawn to you.",
            "",
            "Missed arrows",
            "reappear."
        )

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val proj = event.entity as? AbstractArrow ?: return
        proj.persistentDataContainer.set(projectileKey, PersistentDataType.BYTE, 1)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val proj = event.entity as? AbstractArrow ?: return
        if (!proj.persistentDataContainer.has(projectileKey, PersistentDataType.BYTE)) return

        val shooter = proj.shooter as? Player ?: return
        if (!shooter.isOnline) return

        val hit = event.hitEntity as? LivingEntity
        if (hit != null) {
            if (!hit.isValid) return
            if (hit == shooter) return
            attractTeleport(shooter, hit)
            return
        }

        proj.remove()
        refundMissedArrow(shooter, proj.location, hitBlock = event.hitBlock != null)
    }

    private fun attractTeleport(shooter: Player, target: LivingEntity) {
        val from = target.location.clone()
        val shooterLoc = shooter.location.clone()

        val dir = from.toVector().subtract(shooterLoc.toVector()).apply { y = 0.0 }
        val horizDir = if (dir.lengthSquared() < 0.0001) {
            shooterLoc.direction.clone().apply { y = 0.0 }.normalize()
        } else {
            dir.normalize()
        }

        val dest = shooterLoc.clone().add(horizDir.multiply(1.5))
        dest.y = shooterLoc.y

        from.sync {
            from.world?.spawnParticle(Particle.DUST, from.clone().add(0.0, 1.0, 0.0), 18, 0.35, 0.5, 0.35, 0.02, DUST)
            from.world?.spawnParticle(Particle.END_ROD, from.clone().add(0.0, 1.0, 0.0), 10, 0.25, 0.4, 0.25, 0.01)
            from.world?.playSound(from, Sound.ENTITY_ALLAY_ITEM_GIVEN, 0.8f, 1.6f)
        }

        target.sync {
            if (!target.isValid) return@sync
            target.teleportAsync(dest).thenAccept { success ->
                if (!success) return@thenAccept
                dest.sync {
                    dest.world?.spawnParticle(Particle.HEART, dest.clone().add(0.0, 1.0, 0.0), 3, 0.25, 0.4, 0.25, 0.0)
                    dest.world?.spawnParticle(Particle.DUST, dest.clone().add(0.0, 1.0, 0.0), 22, 0.35, 0.55, 0.35, 0.02, DUST)
                    dest.world?.playSound(dest, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.8f)
                }
            }
        }
    }

    private fun refundMissedArrow(player: Player, at: Location, hitBlock: Boolean) {
        if (player.gameMode == GameMode.CREATIVE) return
        if (!hitBlock) return

        val arrow = ItemStack(Material.ARROW, 1)
        val leftover = player.inventory.addItem(arrow)
        if (leftover.isNotEmpty()) {
            at.world?.dropItemNaturally(at, arrow)
        }

        at.sync {
            val fx = at.clone().add(0.0, 0.15, 0.0)
            fx.world?.spawnParticle(Particle.DUST, fx, 14, 0.25, 0.15, 0.25, 0.02, DUST)
            fx.world?.spawnParticle(Particle.END_ROD, fx.clone().add(0.0, 0.10, 0.0), 6, 0.12, 0.08, 0.12, 0.01)
            fx.world?.playSound(fx, Sound.ITEM_BUNDLE_INSERT, 1.0f, 1.35f)
        }
    }
}

@Disable(WorldName.PINATA)
@FireAnyways(Action.PROJECTILE_LAND)
class HeartstringBow : HeartstringBowItemNest() {
    override fun createItem(): Pair<String, ItemStack> {
        return base
            .name("<b><gradient:#F6C1D1:#E8B7E8:#D7C2F2:#F4B6C2>Cupid's Longbow</gradient></b>")
            .material(Material.BOW)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 5,
            )
            .buildPair()
    }
}

// Could add a crossbow variant here