package dev.lumas.lumaitems.items.weapons.hatchet

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.sync
import dev.lumas.lumaitems.util.Executors.syncTimer
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector

class SoulEaterItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#1d2046&lS&#1c273c&lo&#1c2d32&lu&#1b3427&ll &#1a3a1d&lE&#233428&la&#2c2f33&lt&#34293e&le&#3d2349&lr",
            mutableListOf("&#1e1d56D&#271c4ae&#301b3ev&#391b31o&#421a25u&#4b1919r", "&#1f1f3cH&#401c31o&#611926t&#82161b-&#82161bS&#611926w&#401c31a&#1f1f3cp"),
            mutableListOf("\"&#03263a\"&#09243fD&#102243a&#162048r&#1c1e4dk&#231c51n&#291a56e&#30185as&#36165fs &#3c1464l&#431268u&#49106dr&#431464k&#3e175as &#381b51u&#321e48p&#2d223fo&#272535n &#22292cy&#1c2c23o&#16301au&#113310.&#0b3707\"","","§fPress your swap key (F) to swap between","§fa bow and an axe","","§fUpon killing an entity with this weapon, their soul","§fwill drop, press your drop key (Q) to devour the soul", "§fand overheal based on the strength of the soul"),
            Material.NETHERITE_AXE,
            mutableListOf("souleater"),
            mutableMapOf(Enchantment.SHARPNESS to 10, Enchantment.LOOTING to 6, Enchantment.INFINITY to 1, Enchantment.POWER to 5, Enchantment.UNBREAKING to 8, Enchantment.MENDING to 1)
        )
        return Pair("souleater", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val swapHands: PlayerSwapHandItemsEvent? = event as? PlayerSwapHandItemsEvent
        val entityDeathEvent: EntityDeathEvent? = event as? EntityDeathEvent
        val dropItemEvent: PlayerDropItemEvent? = event as? PlayerDropItemEvent

        when (type) {
            Action.SWAP_HAND -> {
                swapWeapon(player, player.inventory.itemInMainHand)
                swapHands!!.isCancelled = true
            }
            Action.ENTITY_DEATH -> {
                soulOrb(entityDeathEvent!!.entity)
            }
            Action.DROP_ITEM -> {
                if (canDevour(player)) {
                    dropItemEvent?.isCancelled = true
                }
            }

            else -> return false
        }
        return true
    }

    fun swapWeapon(player: Player, item: ItemStack) {
        when (item.type) {
            Material.BOW -> item.setType(Material.NETHERITE_AXE)
            Material.NETHERITE_AXE -> item.setType(Material.BOW)
            else -> return
        }
        player.inventory.setItemInMainHand(item)
    }

    fun soulOrb(entity: LivingEntity) {
        val armorStand = entity.world.spawnEntity(entity.location, EntityType.ARMOR_STAND) as ArmorStand
        armorStand.setMetadata("SolItem", FixedMetadataValue(instance(), "SolItem"))
        armorStand.setMetadata("SoulOrb", FixedMetadataValue(instance(), "SoulOrb"))
        armorStand.isInvisible = true
        armorStand.setGravity(false)
        armorStand.maxHealth = entity.maxHealth

        var count = 0
        Executors.asyncTimer(0, 1) { task ->
            armorStand.world.spawnParticle(Particle.SCULK_SOUL, armorStand.location, 5, 0.5, 0.5, 0.5, 0.1)
            armorStand.world.spawnParticle(Particle.REVERSE_PORTAL, armorStand.location, 5, 0.5, 0.5, 0.5, 0.1)

            armorStand.sync {
                armorStand.world.playSound(armorStand.location, Sound.PARTICLE_SOUL_ESCAPE, 0.5f, 0.5f)
            }

            if (armorStand.isDead || ++count > 80) {
                task.cancel()
            }
        }
    }

    fun canDevour(p: Player): Boolean {
        for (entity in p.getNearbyEntities(50.0, 50.0, 50.0)) {
            if (entity.hasMetadata("SoulOrb")) {
                entity.remove()
                devour(entity as LivingEntity, p)
                return true
            }
        }
        return false
    }

    private fun devour(entity: LivingEntity, p: Player) {
        //im not good at math
        var count = 0
        entity.syncTimer(0, 1) { task ->
            if (++count > 80) {
                task.cancel()
                return@syncTimer
            }

            val loc1 = entity.location.add(0.0, 1.0, 0.0)
            val loc2 = p.location.add(0.0, 1.0, 0.0)
            val vector: Vector = BukkitVectors.direction(loc1, loc2)
            var i = 1.0
            while (i <= loc1.distance(loc2)) {
                vector.multiply(i)
                loc1.add(vector)
                loc1.getWorld().spawnParticle(Particle.SCULK_SOUL, loc1, 1, 0.2, 0.1, 0.2, 0.0)
                loc1.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc1, 1, 0.2, 0.1, 0.2, 0.1)
                loc1.subtract(vector)
                vector.normalize()
                i += 0.5
            }
            loc1.getWorld().spawnParticle(Particle.SCULK_SOUL, loc1, 5, 0.5, 0.5, 0.5, 0.1)
            loc1.getWorld().spawnParticle(Particle.REVERSE_PORTAL, loc1, 5, 0.5, 0.5, 0.5, 0.1)
        }


        //sfx
        p.playSound(p.location, Sound.ENTITY_GENERIC_EAT, 1f, 1f)
        p.playSound(p.location, Sound.ENTITY_VEX_CHARGE, 1f, 1f)
        //overHeal player
        val amplifier = (entity.maxHealth / 8).toInt()
        p.removePotionEffect(PotionEffectType.REGENERATION)
        p.removePotionEffect(PotionEffectType.HEALTH_BOOST)
        p.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 80, amplifier))
        p.addPotionEffect(PotionEffect(PotionEffectType.HEALTH_BOOST, 600, amplifier))
    }
}