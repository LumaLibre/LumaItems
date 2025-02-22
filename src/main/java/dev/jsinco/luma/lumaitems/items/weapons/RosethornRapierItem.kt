package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.particles.Particles
import dev.jsinco.luma.lumaitems.shapes.Sphere
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerInteractAtEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.awt.Color
import java.util.UUID

class RosethornRapierItem : CustomItemFunctions() {

    companion object {
        private val cachedDamagers: MutableMap<UUID, RosethornRecord> = mutableMapOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#FF3334>R<#FF4245>o<#FF5156>s<#FF6066>e<#FF6F77>t<#FF737B>h<#FF7680>o<#FF7A84>r<#FF7D89>n <#FF8492>R<#FF8896>a<#FF737E>p<#FF5E65>i<#FF484D>e<#FF3334>r")
            .customEnchants("<#FF4245>Bloody Mess")
            .lore(
                "<#FF6F77>Right-click<white> any entity to",
                "summon a magic circle below",
                "your feet.",
                "",
                "While active, entities killed",
                "within the circle will drop",
                "significantly more loot.",
                "",
                "<red>Cooldown<gray>:<red> 35s"
            )
            .material(Material.NETHERITE_SWORD)
            .tier(Tier.VALENTIDE_2025)
            .persistentData("rosethorn-rapier")
            .vanillaEnchants(
                Enchantment.SHARPNESS to 7,
                Enchantment.SWEEPING_EDGE to 5,
                Enchantment.LOOTING to 5,
                Enchantment.UNBREAKING to 4,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onPlayerInteractEntity(player: Player, event: PlayerInteractAtEntityEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            return
        }
        QuickTasks.addCooldown(this, player.uniqueId, 700L)

        val rosethornRecord = RosethornRecord.of(player)
        cachedDamagers[player.uniqueId] = rosethornRecord
        val d = rosethornRecord.display
        player.playSound(player.location, Sound.ITEM_ELYTRA_FLYING, 0.2f, 0.4f)
        object : BukkitRunnable() {
            var ticksRan = 0
            override fun run() {
                if (ticksRan++ >= 200) {
                    cachedDamagers.remove(player.uniqueId)
                    player.stopSound(Sound.ITEM_ELYTRA_FLYING)
                    this.cancel()
                    return
                }
                Particles.neopaganPentagram(5.0, 0.05, 0.0, d, d)
            }
        }.runTaskTimerAsynchronously(instance(), 0L, 1L)
    }

    override fun onEntityDeath(player: Player, event: EntityDeathEvent) {
        val rosethornRecordedPlayer = cachedDamagers[player.uniqueId] ?: return
        if (!rosethornRecordedPlayer.sphere.isInSphere(event.entity.location)) {
            return
        }

        if (rosethornRecordedPlayer.damageDealt < 200.0) {
            rosethornRecordedPlayer.damageDealt += event.entity.getAttribute(Attribute.MAX_HEALTH)?.value ?: 0.0
            val blend = if (rosethornRecordedPlayer.damageDealt < 130.0) {
                Util.blend(Color.WHITE, Color.RED)
            } else {
                Color.RED
            }
            rosethornRecordedPlayer.display.mixWith(blend)
        }

        event.entity.world.playSound(event.entity.location, Sound.ENTITY_ALLAY_DEATH, 0.5f, 0.8f)
        event.drops.forEach { increaseItemStackAmount(it, rosethornRecordedPlayer.damageDealt) }
    }


    private fun increaseItemStackAmount(itemStack: ItemStack, damageDealt: Double) {
        //itemStack.amount += 1 // base
        when (damageDealt) {
            in 0.0..130.0 -> {
                itemStack.amount += (itemStack.amount / 3)
            }
            else -> {
                itemStack.amount += (itemStack.amount / 2)
            }
        }
    }

    private class RosethornRecord(
        var damageDealt: Double,
        val display: ParticleDisplay,
        val sphere: Sphere
    ) {
        companion object {
            fun of(player: Player): RosethornRecord {
                val particleDisplay = ParticleDisplay.of(Particle.DUST)
                    .withColor(Color.WHITE)
                    .withLocation(player.location)
                return RosethornRecord(0.0, particleDisplay, Sphere(player.location, 5.0, 20.0))
            }
        }
    }
}