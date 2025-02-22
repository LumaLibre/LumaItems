package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.manager.GlowManager
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.UUID

class FlamingHeartsEstoc : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fb4863&lF&#fb4e62&ll&#fb5562&la&#fc5b61&lm&#fc6260&li&#fc685f&ln&#fc6f5f&lg &#fd755e&lH&#fd7c5d&le&#fd825c&la&#fd8859&lr&#fd8d57&lt&#fd9355&ls &#fd9952&lE&#fd9f50&ls&#fda44e&lt&#fdaa4b&lo&#fdb049&lc",
            mutableListOf("&#fd9355Barrock", "&#fd9355Rumble"),
            mutableListOf("Grants resistance at the cost", "of slowness whilst attacking", "", "Right-click to send out an incendiary", "that explodes on impact", "", "&cCooldown: 30s"),
            Material.NETHERITE_SWORD,
            mutableListOf("flamingheartssword"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.SHARPNESS to 8, Enchantment.FIRE_ASPECT to 4, Enchantment.SMITE to 7, Enchantment.UNBREAKING to 10,
                Enchantment.LOOTING to 4, Enchantment.SWEEPING_EDGE to 4)
        )
        item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"
        return Pair("flamingheartssword", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                if (QuickTasks.isOnCooldown(this, player.uniqueId)) return false

                val fireBall: Fireball = player.launchProjectile(Fireball::class.java)
                fireBall.yield = 0.0f
                fireBall.persistentDataContainer.set(NamespacedKey(instance(), "flamingheartssword"), PersistentDataType.SHORT, 1)
                GlowManager.setGlowColor(fireBall, NamedTextColor.DARK_RED)
                fireBall.isGlowing = true
                fireBall.setIsIncendiary(false)


                QuickTasks.addCooldown(this, player.uniqueId, 600L) {
                    if (fireBall.isValid) {
                        fireBall.remove()
                    }
                }
            }

            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent

                event.entity.getNearbyEntities(7.3, 7.3, 7.3).forEach {
                    if (it !is LivingEntity || it == player || AbilityUtil.noDamagePermission(player, it)) return@forEach
                    // knockback entity away from fireball
                    it.velocity = it.location.toVector().subtract(event.entity.location.toVector()).add(Vector(0.0,5.0,0.0)).multiply(23.5).normalize()
                    it.fireTicks = 100
                    it.damage(25.0, player)
                }
                event.entity.world.playSound(event.entity.location, Sound.ENTITY_WITHER_SHOOT, 1.0f, 2.0f)
                event.entity.world.spawnParticle(Particle.FLAME, event.entity.location, 25, 0.5, 0.5, 0.5, 0.5)
                event.entity.world.spawnParticle(Particle.EXPLOSION, event.entity.location, 1, 0.0, 0.0, 0.0, 0.0)
                event.entity.world.playSound(event.entity.location, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 2.0f)
            }
            Action.ENTITY_DAMAGE -> {
                player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 220, 3, false, false, true))
                player.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 220, 0, false, false, true))
            }
            else -> return false
        }
        return true
    }

}