package dev.lumas.lumaitems.items.weapons.cutlass

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.syncDelayed
import java.util.function.Consumer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Fireball
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class SunlightBladeItem : CustomItem {

    companion object {
        private val undeads = listOf(EntityType.ZOMBIE, EntityType.SKELETON, EntityType.ZOMBIE_VILLAGER, EntityType.STRAY, EntityType.DROWNED, EntityType.PHANTOM)
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#e3dd45&lS&#e7d240&lu&#ecc73b&ln&#f0bc36&ll&#f4b131&li&#f9a62c&lg&#fd9b27&lh&#fd8a2c&lt &#fc7930&lB&#fc6935&ll&#fc5839&la&#fb473e&ld&#fb3642&le",
            mutableListOf("&#fb473eUndead Hatred", "&#fbd54cS&#fbd34ct&#fcd04ca&#fcce4cr&#fccb4db&#fcc94do&#fdc64du&#fdc44dn&#fdc14dd"),
            mutableListOf("&#e3dd45\"&#e4d745L&#e5d145i&#e6ca45g&#e7c445h&#e7be44t &#e8b844t&#e9b244h&#eaac44e &#eba544s&#ec9f44h&#ed9944a&#ee9344d&#ef8d44o&#ef8643w&#f08043s &#f17a43o&#f27443f &#f36e43t&#f46743h&#f56143e &#f65b43u&#f75543n&#f74f42d&#f84942e&#f94242a&#fa3c42d&#fb3642\"","","&fThis weapon deals significantly more", "&fdamage to mobs that burn in the daylight", "", "&fRight click to summon a star that will", "&ffreeze, damage, and ignite all mobs near it","", "&cCooldown: 15 secs"),
            Material.NETHERITE_SWORD,
            mutableListOf("sunlightblade"),
            mutableMapOf(Enchantment.SHARPNESS to 8, Enchantment.FIRE_ASPECT to 3, Enchantment.LOOTING to 4, Enchantment.UNBREAKING to 10, Enchantment.SWEEPING_EDGE to 5, Enchantment.MENDING to 1)
        )
        return Pair("sunlightblade", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val entityDamageEvent: EntityDamageByEntityEvent? = event as? EntityDamageByEntityEvent
        when (type) {
            Action.ENTITY_DAMAGE -> {
                if (undeads.contains(entityDamageEvent!!.entity.type)) {
                    entityDamageEvent.damage *= 1.5
                }
            }
            Action.RIGHT_CLICK -> {
                starbound(player)
            }
            else -> return false
        }
        return true
    }

    private fun starbound(p: Player) {
        if (QuickTasks.isOnCooldown(this, p)) return
        QuickTasks.addCooldown(this, p, 300L)
        createStar(p.location.add(0.0, 3.0, 0.0))
        p.getNearbyEntities(12.0, 10.0, 12.0).forEach(Consumer { entity: Entity ->
            if (entity is LivingEntity) {
                if (entity is ArmorStand || AbilityUtil.noDamagePermission(p, entity)) return@Consumer
                entity.setFireTicks(100)
                entity.damage(11.0, p)
                if (entity is Player) return@Consumer
                entity.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS, 100, 49, false, false))
            }
        })

    }


    private fun createStar(loc: Location) {
        val fireball = loc.getWorld().spawnEntity(loc, EntityType.FIREBALL) as Fireball
        val armorStand = loc.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND) as ArmorStand
        armorStand.isInvisible = true
        armorStand.isInvulnerable = false
        armorStand.setGravity(false)
        armorStand.addPassenger(fireball)
        fireball.isGlowing = true
        fireball.world.playSound(fireball.location, Sound.ITEM_FIRECHARGE_USE, 1f, 1f)

        //val colors = listOf(ChatColor.YELLOW, ChatColor.GOLD, ChatColor.RED)
        //val rand = Random().nextInt(3)
        //GlowManager.setGlowColor(fireball, colors[rand])
        armorStand.syncDelayed(100) {
            armorStand.remove()
            fireball.remove()
        }
    }

}