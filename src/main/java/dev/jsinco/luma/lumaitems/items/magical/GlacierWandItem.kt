package dev.jsinco.luma.lumaitems.items.magical

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.NeedsEdits
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

@NeedsEdits
class GlacierWandItem : CustomItemFunctions() {

    companion object {
        private val cooldown: MutableSet<UUID> = mutableSetOf()
        private val key = Util.namespacedKey("glacierwand")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#C18DF5>G<#C784E9>l<#CD7BDE>a<#D372D2>c<#D969C7>i<#DF5FBB>e<#E556AF>r <#DC6F8B>M<#D28485>a<#C89A7E>g<#BDAF78>i<#B3C471>c <#A8DA6B>W<#9EEF64>a<#F24195>n<#F33B89>d</b>")
            .lore("Unleash a powerful magical explosion!", "Right-click to cast.")
            .material(Material.STICK)
            .customEnchants("<#F24195>Chroma I")
            .vanillaEnchants(
                Enchantment.UNBREAKING to 3,
                Enchantment.EFFICIENCY to 4,
                Enchantment.MENDING to 1
            )
            .tier("<b><#F24195>K<#F33B89>a<#F3367C>t<#F43070>a<#F52A64>r<#F52557>a<#F61F4B>y <#F42D69>2<#F43377>0<#F33A86>2<#F24195>5</b>")
            .persistentData("glacierwand")
            .buildPair()
    }

    private fun magicGlacierExplosion(player: Player) {
        if (cooldown.contains(player.uniqueId)) return

        val snowball = player.launchProjectile(Snowball::class.java)
        snowball.setGravity(false)
        snowball.velocity = player.location.direction.multiply(0.16).normalize()
        snowball.persistentDataContainer.set(key, PersistentDataType.SHORT, 1)
        snowball.setMetadata("MAGIC_BLUE_EXPLOSION", FixedMetadataValue(instance(), "MAGIC_BLUE_EXPLOSION"))
        snowball.shooter = player

        cooldown.add(player.uniqueId)
        Bukkit.getServer().scheduler.scheduleSyncDelayedTask(instance(), { cooldown.remove(player.uniqueId) }, 160L)

        object : BukkitRunnable() {
            override fun run() {
                if (snowball.isDead || snowball.location.block.type.isSolid) {
                    cancel()
                    magicGlacierExplosionLand(snowball)
                    snowball.remove()
                    return
                }

                snowball.world.spawnParticle(Particle.SOUL_FIRE_FLAME, snowball.location, 5, 0.3, 0.3, 0.3, 0.2)
                snowball.world.spawnParticle(Particle.GLOW, snowball.location, 5, 0.3, 0.3, 0.3, 0.2)
            }
        }.runTaskTimer(instance(), 0L, 1L)
    }

    private fun magicGlacierExplosionLand(snowball: Snowball) {
        snowball.world.playSound(snowball.location, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.2f)
        snowball.world.playSound(snowball.location, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 4f, 1f)
        snowball.world.spawnParticle(Particle.EXPLOSION, snowball.location, 1, 0.0, 0.0, 0.0, 0.0)
        snowball.world.spawnParticle(Particle.SOUL_FIRE_FLAME, snowball.location, 100, 0.5, 0.5, 0.5, 0.8)

        snowball.getNearbyEntities(10.0, 10.0, 10.0).mapNotNull { it as? LivingEntity }.forEach {
            if (it != snowball.shooter && it !is Player) {
                it.damage(60.0, snowball.shooter as Player)
            }
        }
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
            magicGlacierExplosion(player)
    }
}
