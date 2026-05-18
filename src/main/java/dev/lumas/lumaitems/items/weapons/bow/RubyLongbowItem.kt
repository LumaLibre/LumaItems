package dev.lumas.lumaitems.items.weapons.bow

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.syncDelayed
import java.util.Random
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class RubyLongbowItem : CustomItem {
    

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#ff5372&lR&#f5518e&lu&#ec4faa&lb&#e24ec5&ly &#d94ce1&lL&#cf4afd&lo&#bd4cfd&ln&#ab4efc&lg&#9950fc&lb&#8752fb&lo&#7554fb&lw",
            mutableListOf("&#ff5372V&#f85286e&#f1509al&#ea4faeo&#e44ec1c&#dd4dd5i&#d64be9t&#cf4afdy"),
            mutableListOf("&#ff5372\"&#fd5378A&#fb527es &#f95284f&#f7518aa&#f55190s&#f25196t &#f0509ca&#ee50a2s &#ec4fa8a &#ea4faes&#e84fb4h&#e64ebbo&#e44ec1o&#e24ec7t&#e04dcdi&#de4dd3n&#dc4cd9g &#d94cdfs&#d74ce5t&#d54beba&#d34bf1r&#d14af7!&#cf4afd\"","","&fArrows fired from this bow will","&ftravel faster and deal more damage","","&fAny arrow fired has 50% chance","&fto replicate itself"),
            Material.BOW,
            mutableListOf("rubylongbow"),
            mutableMapOf(Enchantment.POWER to 5, Enchantment.INFINITY to 1, Enchantment.UNBREAKING to 9, Enchantment.MENDING to 1)
        )
        return Pair("rubylongbow", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val projectileLaunch: ProjectileLaunchEvent? = event as? ProjectileLaunchEvent
        val projectileHit: ProjectileHitEvent? = event as? ProjectileHitEvent

        when (type) {
            Action.PROJECTILE_LAUNCH -> {
                velocity(player, projectileLaunch!!.entity)
            }
            Action.PROJECTILE_LAND -> {
                despawnArrow(projectileHit!!.entity)
            }
            else -> return false
        }
        return true
    }


    @Suppress("DEPRECATION")
    fun velocity(player: Player, proj: Projectile) {
        if (player.hasMetadata("RubyLongbow")) return
        val proj2: Projectile?
        proj.velocity = proj.velocity.multiply(2)
        if (Random().nextBoolean()) {
            player.setMetadata("RubyLongbow", org.bukkit.metadata.FixedMetadataValue(instance(), true))
            proj2 = player.launchProjectile(proj.javaClass, proj.velocity.multiply(0.8))
            player.removeMetadata("RubyLongbow", instance())
            proj2.setMetadata("no-pickup", org.bukkit.metadata.FixedMetadataValue(instance(), true))
            proj2.persistentDataContainer.set(NamespacedKey(instance(), "rubylongbow"), PersistentDataType.SHORT, 1)
        } else {
            proj2 = null
        }


        var count = 0
        Executors.asyncTimer(0, 1) {
            if (++count > 45) {
                it.cancel()
                return@asyncTimer
            }
            proj.world.spawnParticle(Particle.DUST, proj.location, 1, 0.0, 0.0, 0.0, 0.1, DustOptions(Color.fromRGB(255, 83, 114), 1f))
            proj.world.spawnParticle(Particle.DUST, proj.location, 1, 0.0, 0.0, 0.0, 0.1, DustOptions(Color.fromRGB(207, 74, 253), 1f))
            if (proj2 != null) {
                proj2.world.spawnParticle(Particle.DUST, proj2.location, 1, 0.0, 0.0, 0.0, 0.1, DustOptions(Color.fromRGB(255, 83, 114), 1f))
                proj2.world.spawnParticle(Particle.DUST, proj2.location, 1, 0.0, 0.0, 0.0, 0.1, DustOptions(Color.fromRGB(207, 74, 253), 1f))
            }
        }
    }


    private fun despawnArrow(arrow: Projectile) {
        arrow.syncDelayed(20) {
            if (arrow.isDead) return@syncDelayed
            arrow.remove()
        }
    }
}