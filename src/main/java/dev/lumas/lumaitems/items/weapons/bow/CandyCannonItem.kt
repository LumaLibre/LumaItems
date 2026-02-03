package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.sync
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable

class CandyCannonItem : CustomItemFunctions() {


    private val candyMaterials: Map<Material, Color> = mapOf(
        Material.RED_WOOL to Color.RED,
        Material.ORANGE_WOOL to Color.ORANGE,
        Material.YELLOW_WOOL to Color.YELLOW,
    )

    private val particleDisplay: ParticleDisplay = ParticleDisplay.of(Particle.DUST).withColor(java.awt.Color.WHITE).mixWith(
        java.awt.Color.RED)

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><#A7E7FF>C<#B3D6F0>a<#BFC5E0>n<#CBB4D1>d<#D6A3C1>y <#EE81A2>C<#EEAB70>a<#EED53D>n<#D7B874>n<#C19BAC>o<#AA7EE3>n</b>")
            .customEnchants("<#A7E7FF>Sweet Tooth")
            .lore("No lore yet")
            .material(Material.CROSSBOW)
            .persistentData("candycannon")
            .tier(Tier.CARNIVAL_2024)
            .buildPair()
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val arrow = event.entity
        val itemDisplay = arrow.world.spawn(arrow.location, ItemDisplay::class.java)

        val rand = candyMaterials.entries.random()
        itemDisplay.setItemStack(ItemStack(rand.key))
        itemDisplay.displayWidth = 0.2f
        itemDisplay.displayHeight = 0.2f


        arrow.persistentDataContainer.set(NamespacedKey(instance(), "candycannon"), PersistentDataType.SHORT, 1)
        player.hideEntity(instance(), arrow)


        // Despawn if it doesn't land
        var ticks = 0
        Executors.asyncTimer(0, 1) {
            itemDisplay.teleportAsync(arrow.location)
            itemDisplay.world.spawnParticle(Particle.DUST, itemDisplay.location, 2, 0.0, 0.0, 0.0, 1.0, Particle.DustOptions(rand.value, 1.0f))

            if (arrow.isDead || itemDisplay.isDead || ticks++ >= 300) {
                it.cancel()
                arrow.sync {
                    itemDisplay.remove()
                    arrow.remove()
                }
            }
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val arrow = event.entity
        arrow.remove()

        if (event.hitEntity != null) {
            val hit = event.hitEntity as? LivingEntity ?: return
            hit.damage(5.0, player)
            Particles.line(player.location, hit.location, 0.2, particleDisplay)
        }
    }
}