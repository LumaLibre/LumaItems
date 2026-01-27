package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.UUID

class SpringVaultItem : CustomItem {

    companion object {
        private val cooldown: MutableSet<UUID> = mutableSetOf()
        private val plugin = LumaItems.getInstance()

        private val colors: List<Color> = listOf(
            Util.hex2BukkitColor("#6CF380"),
            Util.hex2BukkitColor("#FD9A59"),
            Util.hex2BukkitColor("#EB719C"),
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#6CF380&lS&#82ED80&lp&#98E77F&lr&#ADE17F&li&#C3DB7E&ln&#D9D57E&lg &#DDC184&lV&#E0AD8A&la&#E49990&lu&#E78596&ll&#EB719C&lt",
            mutableListOf("&7Launch I"),
            mutableListOf("Right-click to be propelled", "into the air!", "", "&cCooldown: 15s"),
            Material.STICK,
            mutableListOf("springvault"),
            mutableMapOf(Enchantment.UNBREAKING to 10, Enchantment.KNOCKBACK to 4)
        )
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("springvault", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                if (cooldown.contains(player.uniqueId)) {
                    return false
                }

                player.velocity = player.velocity.add(Vector(0.0, 1.5, 0.0)).multiply(2.45)
                player.playSound(player.location, Sound.ENTITY_ENDER_DRAGON_FLAP, 1f, 1f)

                cooldown.add(player.uniqueId)
                Bukkit.getServer().scheduler.scheduleSyncDelayedTask(plugin, { cooldown.remove(player.uniqueId) }, 300L)

                object : BukkitRunnable() {
                    private var ticks = 0

                    override fun run() {
                        player.world.spawnParticle(Particle.DUST, player.location, 2, 0.3, 0.3, 0.3, 0.1, DustOptions(colors.random(), 1f))
                        ticks++
                        if (ticks >= 50) {
                            this.cancel()
                        }
                    }
                }.runTaskTimer(plugin, 0L, 1L)

            }
            else -> return false
        }
        return true
    }
}