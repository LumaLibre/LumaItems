package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.manager.FileManager
import dev.jsinco.luma.lumaitems.manager.GlowManager
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Vector3f
import java.util.*

class YolkPlaidYataghanItem : CustomItem {

    companion object {
        private val eggTextures: List<String> = FileManager("heads.yml").generateYamlFile().getStringList("easter-egg")
        private val plugin: LumaItems = LumaItems.getInstance()
        private val coolingDownEggs: MutableMap<UUID, Int> = mutableMapOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#FDDF86&lY&#FEE290&lo&#FEE59A&ll&#FFE8A4&lk&#FFEBAE&lp&#F6EFAD&ll&#EEF2AB&la&#E5F6AA&li&#DCF9A8&ld &#D2F9A8&lY&#C8F9A8&la&#BDF9A8&lt&#B3F9A8&la&#A0F6A8&lg&#8DF4A8&lh&#7AF1A7&la&#67EEA7&ln",
            mutableListOf("&#dcf9a8Egg Castor"),
            mutableListOf("While in range, right-click to encase", "up to 2 entities in eggs for a", "short duration, dealing damage", "", "&cCooldown: 20s per egg"),
            Material.NETHERITE_SWORD,
            mutableListOf("yolkplaidyataghan"),
            mutableMapOf(Enchantment.SHARPNESS to 8, Enchantment.UNBREAKING to 9, Enchantment.SMITE to 6, Enchantment.SWEEPING_EDGE to 5, Enchantment.FIRE_ASPECT to 4, Enchantment.MENDING to 1)
        )
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("yolkplaidyataghan", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                val livingEntity: LivingEntity = player.getTargetEntity(35) as? LivingEntity ?: return false
                if (livingEntity is Player) {
                    return false
                } else if (AbilityUtil.noDamagePermission(player, livingEntity)) {
                    return false
                }
                if (cooldownTaskPlayer(player)) {
                    trapMobInEgg(livingEntity, player)
                }
            }
            Action.ENTITY_MOVE, Action.ENTITY_TELEPORT -> {
                event as Cancellable
                event.isCancelled = true
            }
            else -> return false
        }
        return true
    }

    private fun trapMobInEgg(livingEntity: LivingEntity, attacker: Player) {
        val loc = livingEntity.eyeLocation.add(0.0, 0.5, 0.0); loc.yaw = 0.0f; loc.pitch = 0.0f
        val boundingBox = livingEntity.boundingBox
        val egg = livingEntity.world.spawnEntity(loc, EntityType.ITEM_DISPLAY) as ItemDisplay

        egg.isPersistent = false
        egg.setItemStack(Util.playerHeadFromBase64(eggTextures.random(), 1))
        egg.interpolationDuration = 0
        egg.interpolationDelay = -1

        val transformation: Transformation = egg.transformation
        transformation.scale.set(Vector3f(boundingBox.widthX.toFloat(), boundingBox.height.toFloat(), boundingBox.widthZ.toFloat()).mul(3.0f))
        egg.transformation = transformation




        livingEntity.isCollidable = false
        livingEntity.persistentDataContainer.set(NamespacedKey(plugin, "yolkplaidyataghan"), PersistentDataType.SHORT, 1)
        livingEntity.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 240, 0, false, false, false))
        GlowManager.addToTeamForTicks(livingEntity, ChatColor.RED, 240)
        object: BukkitRunnable() {
            var totalTicks = 0

            override fun run() {
                livingEntity.damage(4.0, attacker)
                livingEntity.world.playSound(livingEntity.location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 2f, 7.7f)

                var stop = false
                if (totalTicks >= 240) {
                    livingEntity.isCollidable = true
                    stop = true
                } else if (livingEntity.isDead) {
                    stop = true
                }

                if (stop) {
                    livingEntity.persistentDataContainer.remove(NamespacedKey(plugin, "yolkplaidyataghan"))
                    egg.remove()
                    this.cancel()
                }
                totalTicks+=20
            }
        }.runTaskTimer(plugin, 0L, 20L)
    }

    private fun cooldownTaskPlayer(player: Player): Boolean {
        val amountOfCoolingDownEggs = coolingDownEggs[player.uniqueId] ?: 0
        if (amountOfCoolingDownEggs >= 2) {
            return false
        } else {
            coolingDownEggs[player.uniqueId] = amountOfCoolingDownEggs + 1
            object: BukkitRunnable() {
                override fun run() {
                    var coolingDownEggsAmount = coolingDownEggs[player.uniqueId] ?: return
                    coolingDownEggsAmount -= 1

                    if (coolingDownEggsAmount > 0) {
                        coolingDownEggs[player.uniqueId] = coolingDownEggsAmount
                    } else {
                        coolingDownEggs.remove(player.uniqueId)
                    }
                }
            }.runTaskLater(plugin, 400)
        }
        return true
    }


}