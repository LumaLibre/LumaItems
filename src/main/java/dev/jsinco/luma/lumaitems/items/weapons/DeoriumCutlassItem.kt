package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.items.weapons.OriginalDeoriumCutlassItem.Companion.plugin
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

class DeoriumCutlassItem : CustomItemFunctions() {

    companion object {
        private val colors: List<Color> = listOf(
            "#cab1d1", "#fde6f0", "#fffff8", "#7f839c", "#adbfd2"
        ).map { Util.hex2BukkitColor(it) }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#CAB1D1>D<#D7BED9>e<#E4CCE1>o<#F0D9E8>r<#FDE6F0>i<#FEEEF3>u<#FEF7F5>m <#DFE0E1>C<#BFC1CA>u<#9FA2B3>t<#7F839C>l<#8E97AE>a<#9EABC0>s<#ADBFD2>s")
            .customEnchants("<#CAB1D1>Event Horizon")
            .lore("Right-click to summon a gravity", "well at a targeted block." , "", "Entities nearby the well will", "be damaged and weakened.", "", "<red>Cooldown: 30s")
            .material(Material.NETHERITE_SWORD)
            .persistentData("deoriumcutlass")
            .vanillaEnchants(Enchantment.SHARPNESS to 8, Enchantment.SMITE to 8, Enchantment.LOOTING to 5, Enchantment.SWEEPING_EDGE to 4, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1)
            .tier(Tier.WINTER_2024)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) return
        val block = player.getTargetBlockExact(50) ?: return
        createPullVoid(block.location.add(0.0,1.0,0.0), player)
        QuickTasks.addCooldown(this, player.uniqueId, 600L)
    }


    private fun createPullVoid(location: Location, p: Player) {
        val points = 50
        val step = 60
        val color = colors.random()
        val armorStand = location.world.createEntity(location, ArmorStand::class.java)
        armorStand.isVisible = false
        armorStand.isSmall = true
        armorStand.spawnAt(location)

        object : BukkitRunnable() {
            var ticksRan = 0

            override fun run() {
                for (entity in armorStand.getNearbyEntities(10.0,10.0,10.0)) {
                    if (entity.type == EntityType.ARMOR_STAND || entity.type == EntityType.PLAYER || AbilityUtil.noDamagePermission(p, entity)) continue
                    val direction: Vector = armorStand.location.subtract(entity.location).toVector()
                    val distance: Double = entity.location.distance(armorStand.location)

                    if (distance <= 2.5 && entity is LivingEntity) {
                        entity.damage(5.0, p)
                        entity.velocity = Vector(0, 0, 0)

                        entity.addPotionEffect(PotionEffect(PotionEffectType.WEAKNESS, 40, 1, false, false, false))
                        entity.world.spawnParticle(
                            Particle.DUST, entity.location, 5, 0.6, 0.6, 0.6, 0.8,
                            Particle.DustOptions(color, 1f)
                        )
                    }
                    entity.velocity = direction.normalize().multiply(distance / 20)
                }

                for (i in 0 until points) {
                    val dx: Double = cos(step + Math.PI * 2 * (i.toDouble() / points))
                    val dz: Double = sin(step + Math.PI * 2 * (i.toDouble() / points))
                    armorStand.location.world.spawnParticle(Particle.INSTANT_EFFECT, armorStand.location.x + dx, armorStand.location.y, armorStand.location.z + dz, 1, 0.0, 0.0, 0.0, 0.1)
                    armorStand.location.world.spawnParticle(
                        Particle.DUST, armorStand.location.x + dx, armorStand.location.y, armorStand.location.z + dz, 1, 0.0, 0.0, 0.0, 0.5, Particle.DustOptions(
                            color, 1f))
                }
                armorStand.location.world.playSound(armorStand.location, Sound.ENTITY_WITHER_AMBIENT, 0.08f, 2f)

                ticksRan += 5
                if (ticksRan >= 120) {
                    armorStand.remove()
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 5L)
    }
}