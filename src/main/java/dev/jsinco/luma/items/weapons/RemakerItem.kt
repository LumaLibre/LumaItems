package dev.jsinco.luma.items.weapons

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import dev.jsinco.luma.manager.GlowManager
import dev.jsinco.luma.util.AbilityUtil
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class RemakerItem : CustomItem {

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
        private val dustOptions = DustOptions(Color.BLACK, 1f)
        private val cooldown: MutableList<Player> = mutableListOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#3448b1&lR&#3b48b4&le&#4247b7&lm&#4947ba&la&#5046bd&lk&#5746c0&le&#5e45c3&lr",
            mutableListOf("&#2c1c6aP&#2d2271a&#2e2778r&#2f2d7fa&#313386n&#32398do&#333e94i&#34449ba"),
            mutableListOf("&#160E35\"See nothing.\"","","§fHolding this dagger will passively spawn","§fdark auras around opponents causing","§fthem to waste away","","§fRight click to unleash a bad omen, highlighting", "§fnearby opponents and blinding them","","§cCooldown: 23 secs"),
            Material.NETHERITE_SWORD,
            mutableListOf("remaker"),
            mutableMapOf(Enchantment.SHARPNESS to 8, Enchantment.SMITE to 9, Enchantment.SWEEPING_EDGE to 4, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1)
        )
        return Pair("remaker", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                paranoia(player)
            }
            Action.RIGHT_CLICK -> {
                nearSightEnemies(player)
            }
            else -> return false
        }
        return true
    }

    private fun paranoia(p: Player) {
        p.getNearbyEntities(10.0, 10.0, 10.0).forEach { entity ->
            if (entity !is LivingEntity || AbilityUtil.noDamagePermission(p, entity)) return@forEach
            if (entity is Monster || entity is Player) {
                entity.world.spawnParticle(Particle.DUST, entity.location.add(0.0, 1.0, 0.0), 150, 0.5, 0.5, 0.5, 0.1, dustOptions)
                entity.damage(1.5)
                entity.velocity = Vector(0, 0, 0)
            }
        }
    }

    private fun nearSightEnemies(p: Player) {
        if (cooldown.contains(p)) return
        cooldown.add(p)
        p.getNearbyEntities(10.0, 10.0, 10.0).forEach { entity ->
            if (entity !is LivingEntity || AbilityUtil.noDamagePermission(p, entity)) return@forEach
            entity.world.spawnParticle(Particle.DUST, entity.eyeLocation, 300, 0.2, 0.1, 0.2, 0.1, dustOptions)
            entity.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, 100, 0))
            entity.addPotionEffect(PotionEffect(PotionEffectType.LEVITATION, 100, 0))
            entity.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 100, 0))

            GlowManager.addToTeamForTicks(entity, ChatColor.BLACK, 100)

            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
                cooldown.remove(p)
            }, 460L)
        }
    }
}