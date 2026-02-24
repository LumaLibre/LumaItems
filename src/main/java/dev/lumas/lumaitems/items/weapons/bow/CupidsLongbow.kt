package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.extensions.dustOptions
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setPersistentKey
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Disable(WorldName.PINATA)
class CupidsLongbow : CustomItemFunctions() {

    private companion object {
        private val KEY = "cupids-longbow".namespacedKey()
        private val DUST = "#f5a6c2".dustOptions()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#E85C6F:#F29AA8:#F59CAB:#F07A8A>Cupid's Longbow</gradient></b>")
            .customEnchants("<#f5a6c2>Attraction")
            .material(Material.BOW)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 5,
                Enchantment.MENDING to 1
            )
            //.tagline("#f5a6c2", "Love pulls gently, but surely.")
            .lore(
                "Entities <#f5a6c2>hit</#f5a6c2> by arrows",
                "from this bow will be",
                "drawn toward you.",
                "",
                "Any <#f5a6c2>missed</#f5a6c2> arrows",
                "will be drawn back",
                "towards you."
            )
            .buildPair()
    }


    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val proj = event.entity as? AbstractArrow ?: return
        proj.setPersistentKey(KEY, PersistentDataType.SHORT, 1)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val proj = event.entity as? AbstractArrow ?: return

        val shooter = proj.shooter as? Player ?: return


        proj.world.spawnParticle(Particle.DUST, proj.location.add(0.0, 1.0, 0.0), 18, 0.35, 0.5, 0.35, 0.02, DUST)
        proj.world.spawnParticle(Particle.END_ROD, proj.location.add(0.0, 1.0, 0.0), 10, 0.25, 0.4, 0.25, 0.01)

        val hit = (event.hitEntity as? LivingEntity)?.takeIf { it !is Player }
        if (hit != null) {
            player.damageItemStack(player.inventory.itemInMainHand, 10)
            attractTeleport(shooter, hit)
            return
        }

        if (event.hitBlock == null) {
            return
        }

        proj.syncDelayed(20) {
            if (proj.isDead) {
                return@syncDelayed
            }

            proj.setNoPhysics(true)

            var count = 0
            proj.syncTimer(0,1) { task ->
                val distance = player.eyeLocation.distanceSquared(proj.location)
                if (distance < 0.5 * 0.5 || proj.isDead || ++count > 200) {
                    task.cancel()
                    return@syncTimer
                }

                proj.velocity = BukkitVectors.flyToLivingEntity(player, proj, 2.8, 0.9, 0.89)
            }
        }
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

        from.world?.playSound(from, Sound.ENTITY_ALLAY_ITEM_GIVEN, 0.8f, 1.6f)

        target.teleportAsync(dest).thenAccept { success ->
            if (!success) return@thenAccept
            dest.world?.spawnParticle(Particle.HEART, dest.clone().add(0.0, 1.0, 0.0), 3, 0.25, 0.4, 0.25, 0.0)
            dest.world?.spawnParticle(Particle.DUST, dest.clone().add(0.0, 1.0, 0.0), 22, 0.35, 0.55, 0.35, 0.02, DUST)
            dest.sync {
                dest.world?.playSound(dest, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.9f, 1.8f)
            }
        }
    }

}