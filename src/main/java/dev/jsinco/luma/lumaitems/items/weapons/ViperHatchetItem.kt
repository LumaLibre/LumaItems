package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityCategory
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.Random

class ViperHatchetItem : CustomItem {

    companion object {
        val plugin: LumaItems = LumaItems.getInstance()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#a2fc3d&lV&#9bf23c&li&#94e83a&lp&#8ddf39&le&#86d537&lr &#7fcb36&lH&#77c034&la&#6eb431&lt&#64a82e&lc&#5a9c2b&lh&#519029&le&#478426&lt",
            mutableListOf("&#e4fc52V&#d4fc4de&#c3fc48n&#b3fc42o&#a2fc3dm","&#e4fc52F&#d4fc4da&#c3fc48n&#b3fc42g&#a2fc3ds"),
            mutableListOf("&#478426\"&#4a8827T&#4d8c28h&#509028e &#539429v&#56982ae&#599c2bn&#5ca02bo&#5fa42cm &#62a82dh&#65ac2ea&#68b02ei&#6bb42fl&#6eb830s &#71bc31n&#75c032o&#78c432t &#7bc833f&#7ecc34r&#81d035o&#84d435m &#87d836t&#8adc37h&#8de038i&#90e438s &#93e839r&#96ec3ae&#99f03ba&#9cf43bl&#9ff83cm&#a2fc3d\"","","&fEntities attacked with this weapon","&fwill take poison damage","","&fOpponents will have a chance to be bitten","&fand take extra damage"),
            Material.NETHERITE_AXE,
            mutableListOf("viperhatchet"),
            mutableMapOf(Enchantment.SHARPNESS to 8, Enchantment.UNBREAKING to 10, Enchantment.MENDING to 1)
        )
        return Pair("viperhatchet", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val entityDamageEvent: EntityDamageByEntityEvent? = event as? EntityDamageByEntityEvent

        when (type) {
            Action.ENTITY_DAMAGE -> {
                entityDamageEvent?.damage = viper(entityDamageEvent!!.entity, entityDamageEvent.damage, player)
            }
            else -> return false
        }
        return true
    }


    private fun viper(e: Entity, damage: Double, player: Player): Double {
        if (e !is LivingEntity) return damage
        if (e.category == EntityCategory.UNDEAD && !e.hasMetadata("viper")) {
            e.setMetadata("viper", FixedMetadataValue(plugin, true))
            val repeatTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin,
                { e.damage(0.5) }, 0L, 15L
            )
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
                Bukkit.getScheduler().cancelTask(repeatTask)
                e.removeMetadata("viper", plugin)
            }, 100L)
        }
        if (!e.hasPotionEffect(PotionEffectType.POISON)) {
            e.addPotionEffect(PotionEffect(PotionEffectType.POISON, 100, 1, false, false, true))
        }
        val chance: Int = if (player.scoreboardTags.contains("lumaitems.debug")) {
            1
        } else {
            Random().nextInt(100)
        }
        if (chance <= 15) {
            for (i in 0..4) {
                e.getWorld()
                    .spawnParticle(Particle.SWEEP_ATTACK, e.getLocation().add(0.0, 1.0, 0.0), 1, 0.5, 0.5, 0.5, 0.1)
                e.getWorld().playSound(e.getLocation(), Sound.ITEM_AXE_STRIP, 1f, 0.9f)
            }
            return damage * 1.5
        }
        return damage
    }
}