package dev.jsinco.luma.items.weapons

import dev.jsinco.luma.util.tiers.Tier
import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.manager.CustomItemFunctions
import dev.jsinco.luma.shapes.ShapeUtil
import dev.jsinco.luma.util.AbilityUtil
import dev.jsinco.luma.util.Util
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class GiantInflatableHammerItem : CustomItemFunctions() {

    companion object {
        private val colors = listOf(
            Util.hex2BukkitColor("#fca2ab"),
            Util.hex2BukkitColor("#fccba8"),
            Util.hex2BukkitColor("#fbfcb5"),
            Util.hex2BukkitColor("#b0fc9f"),
            Util.hex2BukkitColor("#a3f1fc")
        )
    }



    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#FFA4AD>G<#FFABAD>i<#FFB2AC>a<#FFB9AC>n<#FFBFAB>t <#FFCDAA>I<#FFD7AD>n<#FFE1AF>f<#FEEBB2>l<#FEF5B4>a<#FEFFB7>t<#F1FFB3>a<#E5FFB0>b<#D8FFAC>l<#CBFFA8>e <#B2FFA1>H<#AFFDB4>a<#ADFBC7>m<#AAF8D9>m<#A8F6EC>e<#A5F4FF>r</b>")
            .customEnchants("<#FFA4AD>Astonish")
            .lore("Upon smash attacking, hit", "entities will be stunned.", "", "Occasionally, smash attacks", "with this weapon will push", "entities through the", "ground.")
            .material(Material.MACE)
            .persistentData("giantinflatablehammer")
            .tier(Tier.CARNIVAL_2024)
            .vanillaEnchants(mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 5, Enchantment.DENSITY to 5, Enchantment.SHARPNESS to 3, Enchantment.WIND_BURST to 2))
            .buildPair()
    }

    override fun onMaceSmashAttack(player: Player, event: EntityDamageByEntityEvent) {
        stunEntity(event.entity as? LivingEntity ?: return)

        val loc = event.entity.location
        if (!AbilityUtil.noBuildPermission(player, loc.block) && Random.nextInt(10) == 1) {
            pushThruGround(loc)
        }
    }

    private fun pushThruGround(loc: Location) {
        for (block in ShapeUtil.circle(loc.subtract(0.0, 1.0, 0.0), 2, 7)) {
            block.breakNaturally(true, false)
        }
    }

    private fun stunEntity(entity: LivingEntity) {
        if (entity !is Player) {
            entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 150, 10))
        }
        entity.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 150, 1))
        entity.world.spawnParticle(Particle.DUST, entity.eyeLocation, 30, 0.5, 0.2, 0.5, 0.1, Particle.DustOptions(
            colors.random(), 1.4f))
    }
}


/*override fun executeActions(type: Action, player: Player, event: Any): Boolean {
    when (type) {
        Action.ENTITY_DAMAGE -> {
            event as EntityDamageByEntityEvent

            val entity = event.entity as? LivingEntity ?: return false

            if (event.isCritical && !entity.persistentDataContainer.has(key, PersistentDataType.SHORT) && entity.health > event.damage) {
                entity.world.spawnParticle(
                    Particle.DUST, entity.eyeLocation, 100, 0.3, -1.5, 0.3, 0.1, Particle.DustOptions(colors.random(), 1f)
                )
                addQueueEntityDamage(player, entity, event.damage.toFloat())

                Bukkit.getScheduler().runTaskLater(instance(), Runnable {
                    val damageStore = getQueuedEntityDamage(entity) ?: return@Runnable
                    damageStore.executeAndRemove(player, entity)
                }, 100L)
            } else if (entity.persistentDataContainer.has(key, PersistentDataType.SHORT)) {
                updateQueuedEntityDamage(entity, event.damage.toFloat())
                event.isCancelled = true
            }

        }

        Action.ENTITY_MOVE -> {
            event as EntityMoveEvent
            event.isCancelled = true
        }

        Action.RIGHT_CLICK -> {
            for (damageStore in getQueuedEntityDamages(player)) {
                damageStore.executeAndRemove(player)
            }
        }

        else -> return false
    }
    return true
}

private fun addQueueEntityDamage(player: Player, entity: LivingEntity, damage: Float) {
    entity.persistentDataContainer.set(key, PersistentDataType.SHORT, 1)
    queuedEntityDamages.add(DamageStore(player.uniqueId, entity.uniqueId, damage))
}

private fun getQueuedEntityDamages(player: Player) = queuedEntityDamages.filter { it.player == player.uniqueId }
private fun getQueuedEntityDamage(entity: LivingEntity) = queuedEntityDamages.find { it.entity == entity.uniqueId }

private fun updateQueuedEntityDamage(entity: LivingEntity, damage: Float) {
    val damageStore = queuedEntityDamages.find { it.entity == entity.uniqueId } ?: return
    damageStore.updateDamage(damage)
}

private data class DamageStore(val player: UUID, val entity: UUID, var damage: Float) {

    fun updateDamage(damage: Float) {
        this.damage += damage
    }

    fun executeAndRemove(player: Player, entity: LivingEntity) {
        if (!entity.isDead) {
            entity.persistentDataContainer.remove(key)
            entity.damage(damage.toDouble(), player)
            entity.world.spawnParticle(
                Particle.DUST, entity.eyeLocation, 100, 0.3, -1.5, 0.3, 0.1, Particle.DustOptions(Color.MAROON, 1f)
            )
        }
        queuedEntityDamages.remove(this)
    }

    fun executeAndRemove(player: Player) {
        val entity = Bukkit.getEntity(entity) as? LivingEntity ?: return
        executeAndRemove(player, entity)
    }

    fun executeAndRemove() {
        val entity = Bukkit.getEntity(entity) as? LivingEntity ?: return
        Bukkit.getPlayer(player)?.let { executeAndRemove(it, entity) }
        queuedEntityDamages.remove(this)
    }
}*/