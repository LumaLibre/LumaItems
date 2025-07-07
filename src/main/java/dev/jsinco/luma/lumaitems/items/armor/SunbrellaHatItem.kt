package dev.jsinco.luma.lumaitems.items.armor

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Arrow
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BlockIterator
import org.bukkit.util.Vector

class SunbrellaHatItem : CustomItemFunctions() {

    companion object {
        private val dustOption = DustOptions(Color.WHITE, 1f)
        private const val FALL_MULTIPLIER = 0.5
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#A0F562>S<#B6DD75>u<#CCC487>n<#E2AC9A>b<#F893AC>r<#E5A2BE>e<#D1B0D1>l<#BEBFE3>l<#AACDF5>a<#ABC1F3> <#ABB6F2>H<#ACAAF0>a<#AC9EEE>t")
            .customEnchants("<#B4E591>Breezy Day")
            .material(Material.NETHERITE_HELMET)
            .persistentData("sunbrellahat")
            .tier(Tier.SUMMER_2025)
            .lore(
                "<#F893AC>Tailwind</#F893AC> <gray>-</gray> While falling, <#F893AC>sneak",
                "to glide down slowly, mitigating",
                "fall damage.",
                "",
                "<#AACDF5>Trailblazer</#AACDF5> <gray>-</gray> While wearing,",
                "knockback attacks will launch",
                "enemies further and faster.",
                "",
                "<#AC9EEE>WindTye</#AC9EEE> <gray>-</gray> While wearing,",
                "arrows fired will be altered",
                "to wind slashes that glide",
                "through the air."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 5,
                Enchantment.FEATHER_FALLING to 6,
                Enchantment.PROJECTILE_PROTECTION to 4,
                Enchantment.UNBREAKING to 4,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (!player.isSneaking || player.velocity.y > -0.01 || player.fallDistance < 0.1) {
            return
        }
        val blockIterator = BlockIterator(player.world, player.location.toVector(), Vector(0.0, -1.0, 0.0), 0.0, 2)
        while (blockIterator.hasNext()) {
            if (!blockIterator.next().type.isAir) {
                return
            }
        }


        val vec = player.location.direction.multiply(0.25)
        player.velocity = Vector(vec.x, player.velocity.y * FALL_MULTIPLIER, vec.z)
        player.fallDistance = 0.5f
        player.world.spawnParticle(Particle.DUST, player.location, 4, 0.3, 0.0, 0.3, dustOption)
    }


    override fun onEntityDamage(player: Player, event: EntityDamageByEntityEvent) {
        val livingEntity = event.entity as? LivingEntity ?: return

        // Check for knockback attack
        if (!event.isCritical && player.isSprinting) {
            // Adjust velocity of the attacked entity to knock them back further than normal.
            val vector: Vector = livingEntity.location.toVector().subtract(player.location.toVector()).multiply(1.5).add(Vector(0.0, 0.1, 0.0))

            vector.x = (-3.0).coerceAtLeast(vector.x.coerceAtMost(3.0))
            vector.y = (-3.0).coerceAtLeast(vector.y.coerceAtMost(3.0))
            vector.z = (-3.0).coerceAtLeast(vector.z.coerceAtMost(3.0))
            livingEntity.velocity = vector
            livingEntity.world.spawnParticle(Particle.DUST, livingEntity.location, 10, 0.5, 0.5, 0.5, dustOption)
        }
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val projectile = event.entity as? Arrow ?: return
        projectile.setGravity(false)
        projectile.isPersistent = false
        projectile.persistentDataContainer.set(NamespacedKey(instance(), "sunbrellahat"), PersistentDataType.SHORT, 0)
        player.hideEntity(instance(), projectile)
        for (entity in projectile.getNearbyEntities(100.0, 100.0, 100.0)) {
            if (entity is Player) {
                entity.hideEntity(instance(), projectile)
            }
        }

        var count = 0
        Executors.syncTimer(1, 3) { task ->
            if (projectile.isDead) {
                task.cancel()
                return@syncTimer
            }

            projectile.world.spawnParticle(Particle.SWEEP_ATTACK, projectile.location, 1, 0.0, 0.0, 0.0, 0.0)
            projectile.world.spawnParticle(Particle.CLOUD, projectile.location, 1, 0.1, 0.1, 0.1, 0.0)


            if (count >= 130) {
                projectile.remove()
                task.cancel()
                return@syncTimer
            }
            count+= 3
        }

    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        event.entity.remove()
    }

}