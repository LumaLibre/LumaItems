package dev.jsinco.luma.lumaitems.items.nests

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

abstract class StraszBowItemNest : CustomItemFunctions() {

    companion object {
        const val DUPLICATE = "duplicate"
        const val MAX_DUPLICATE_ARROWS = 20
        private val redDye = ItemStack(Material.RED_DYE).apply { addUnsafeEnchantment(Enchantment.FLAME, 1) }
        private val dustOptions = DustOptions(Color.RED, 1f)
    }

    private val key = NamespacedKey(instance(), "strasz-longbow")
    private val metaDataValue = FixedMetadataValue(instance(), true)

    val baseBow = ItemFactory.builder()
        .customEnchants("<gradient:#cc6e9b:#eb94a6>Replication")
        .persistentData(key.key)
        .tier(Tier.VALENTIDE_2025)
        .lore(
            "Projectiles from this bow",
            "will travel faster and deal",
            "more damage.",
            "",
            "Hit entities will spawn",
            "duplicate arrows that will",
            "seek out nearby entities."
        )

// It looks like Mojang, Paper, or Purpur added infinity support on crossbows natively.
//  Either way, we don't need this for right now!
//    override fun onCrossBowLoad(player: Player, event: EntityLoadCrossbowEvent) {
//        event.setConsumeItem(false)
//    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val arrow = event.entity


        spawnProjectile(arrow.velocity.multiply(1.5), arrow.location, false, player)
        arrow.remove()
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val hitLivingEntity: LivingEntity = event.hitEntity as? LivingEntity ?: return
        val dmg = 10.0 + (hitLivingEntity.health / 10.0)
        // Replicate vanilla behavior - endermen are always immune to projectiles or able to teleport away
        hitLivingEntity.takeIf { it.type != EntityType.ENDERMAN }?.damage(dmg, player)
        hitLivingEntity.world.playSound(hitLivingEntity.location, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 1.8f)


        if (event.entity.hasMetadata(DUPLICATE)) {
            return
        }
        var i = 0
        hitLivingEntity.getNearbyEntities(20.0, 20.0, 20.0).forEach {
            if (it !is LivingEntity || it is Player || !hitLivingEntity.hasLineOfSight(it)) return@forEach
            else if (i++ > MAX_DUPLICATE_ARROWS) return
            val vector: Vector = AbilityUtil.getDirectionBetweenLocations(hitLivingEntity.eyeLocation, it.eyeLocation)
            spawnProjectile(vector.multiply(0.5), hitLivingEntity.eyeLocation.add(0.0,0.4,0.0), true, player, true)
        }
    }

    // a little messy
    private fun spawnProjectile(velocity: Vector, location: Location, gravity: Boolean, shooter: LivingEntity, duplicate: Boolean = false, spawn: Boolean = true): Snowball {
        val snowball = shooter.world.createEntity(location, Snowball::class.java)
        snowball.velocity = velocity
        snowball.setGravity(gravity)
        snowball.item = redDye
        if (spawn) {
            snowball.spawnAt(location, CreatureSpawnEvent.SpawnReason.CUSTOM)
        }
        snowball.persistentDataContainer.set(key, PersistentDataType.SHORT, 1)
        snowball.shooter = shooter
        snowball.isPersistent = false
        if (duplicate) {
            snowball.setMetadata(DUPLICATE, metaDataValue)
        }

        Bukkit.getAsyncScheduler().runAtFixedRate(instance(), { task ->
            if (snowball.isDead || snowball.ticksLived > 110) {
                if (!snowball.isDead && !snowball.hasGravity()) {
                    Bukkit.getScheduler().runTask(instance(), Runnable { snowball.setGravity(true) })
                }
                task.cancel()
                return@runAtFixedRate
            }
            snowball.world.spawnParticle(Particle.DUST, snowball.location, 5, 0.2, 0.2, 0.2, 0.1, dustOptions)
            snowball.world.spawnParticle(Particle.WITCH, snowball.location, 5, 0.2, 0.2, 0.2, 0.1)
        }, 0, 50, TimeUnit.MILLISECONDS)
        return snowball
    }
}


@Disable(WorldName.PINATA)
class StraszLongBow : StraszBowItemNest() {
    override fun createItem() = baseBow
        .name("<b><#A36CA7>S<#B173AF>t<#BE7AB7>r<#CC81BF>a<#CC78AD>s<#CC6E9B>z <#E187A2>L<#EB94A6>o<#F1ADB2>n<#F6C5BD>g<#F6C5BD>b<#F6C5BD>o<#F6C5BD>w")
        .material(Material.BOW)
        .vanillaEnchants(
            Enchantment.POWER to 8,
            Enchantment.LOOTING to 4,
            Enchantment.UNBREAKING to 4,
            Enchantment.MENDING to 1,
            Enchantment.INFINITY to 1
        )
        .buildPair()
}

@Disable(WorldName.PINATA)
class StraszArcBowItem : StraszBowItemNest() {
    override fun createItem() = baseBow
        .name("<b><#AA68A9>S<#C072B5>t<#D67BC1>r<#DD7CB6>a<#E57EAB>s<#EC7FA0>z <#FAA7AF>A<#FAACB1>r<#FBB1B3>c<#FBB7B5>b<#FCBCB7>o<#FCC1B9>w")
        .material(Material.CROSSBOW)
        .vanillaEnchants(
            Enchantment.PIERCING to 7,
            Enchantment.QUICK_CHARGE to 3,
            Enchantment.LOOTING to 4,
            Enchantment.UNBREAKING to 7,
            Enchantment.MENDING to 1,
            Enchantment.INFINITY to 1
        )
        .buildPair()
}