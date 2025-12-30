package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.QuickTasks
import dev.lumas.lumaitems.util.AbilityUtil.isOnGround
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.UUID

class ThunderStridesItem : CustomItem {

    companion object {
        val directions: MutableMap<UUID, Vector> = mutableMapOf()

        val activeFastLane: MutableList<UUID> = mutableListOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fbe734&lT&#fbe344&lh&#fcde53&lu&#fcda63&ln&#fcd573&ld&#fdd182&le&#fdcc92&lr &#f0c1a2&lS&#d6aeb1&lt&#bc9bc0&lr&#a289cf&li&#8876df&ld&#6e64ee&le&#5451fd&ls",
            mutableListOf("&#fbe734F&#fce835a&#fde937s&#d3cb70t &#949fc4L&#677afda&#5d66fdn&#5451fde"),
            mutableListOf("&#fbe734\"&#fbe43fS&#fbe149u&#fcde54r&#fcdb5fg&#fcd86ae &#fcd574l&#fcd27fi&#fdcf8ak&#fdcc95e &#f4c49fL&#e2b7aai&#d1aab4g&#bf9dbeh&#ad91c9t&#9b84d3n&#8977dei&#786ae8n&#665ef3g&#5451fd\"","","Crouch to activate a speed boost", "during your boost, crouch to slide","","&cCooldown: 16 secs"),
            Material.NETHERITE_BOOTS,
            mutableListOf("thunderstrides"),
            mutableMapOf(Enchantment.PROTECTION to 7, Enchantment.PROJECTILE_PROTECTION to 7, Enchantment.FEATHER_FALLING to 8 , Enchantment.UNBREAKING to 10 ,Enchantment.MENDING to 1)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("thunderstrides", item.createItem())
    }



    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.PLAYER_CROUCH -> {
                if (player.isSneaking || QuickTasks.isOnCooldown(this, player)) return false

                if (activeFastLane.contains(player.uniqueId) && isOnGround(player)) {
                    slideAbility(player)
                } else if (!activeFastLane.contains(player.uniqueId)) {
                    startFastLane(player)
                }
            }
            Action.MOVE -> {
                if (!activeFastLane.contains(player.uniqueId)) return false
                val playerMoveEvent: PlayerMoveEvent = event as PlayerMoveEvent
                val vector = playerMoveEvent.to.clone().subtract(playerMoveEvent.from.clone()).toVector()
                directions[player.uniqueId] = vector
            }
            else -> return false
        }
        return true
    }


    private fun startFastLane(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.SPEED, 140, 2, false, false, false))
        activeFastLane.add(player.uniqueId)
        val particles = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance(), {
            player.world.spawnParticle(Particle.DUST, player.location, 1, 0.2, 0.0, 0.2, 0.1, DustOptions(Color.fromRGB(251, 216, 90), 0.9f))
            player.world.spawnParticle(Particle.DUST, player.location, 1, 0.2, 0.0, 0.2, 0.1, DustOptions(Color.fromRGB(106, 129, 253), 0.9f))
        }, 0L, 1L)

        Bukkit.getScheduler().scheduleSyncDelayedTask(instance(), {
            stopFastLane(player)
            Bukkit.getScheduler().cancelTask(particles)
        }, 140L)
    }

    private fun stopFastLane(player: Player) {
        activeFastLane.remove(player.uniqueId)
        QuickTasks.addCooldown(this, player, 320L)
    }

    private fun slideAbility(player: Player) {
        if (player.hasMetadata("thunderstrides")) return

        player.velocity = directions[player.uniqueId]?.multiply(7.5)?.setY(-0.1) ?: return
        player.world.spawnParticle(Particle.WAX_OFF, player.location, 6, 0.5, 0.5, 0.5, 0.3)

        player.setMetadata("thunderstrides", FixedMetadataValue(instance(), true))
        Bukkit.getScheduler().scheduleSyncDelayedTask(instance(), {
            player.removeMetadata("thunderstrides", instance())
        }, 17)
    }
}