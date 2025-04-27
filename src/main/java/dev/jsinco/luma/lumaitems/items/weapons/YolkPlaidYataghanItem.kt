package dev.jsinco.luma.lumaitems.items.weapons

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.manager.FileManager
import dev.jsinco.luma.lumaitems.manager.GlowManager
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import io.papermc.paper.event.entity.EntityMoveEvent
import java.util.UUID
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityTeleportEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Vector3f

class YolkPlaidYataghanItem : CustomItemFunctions() {

    companion object {
        private val eggTextures: List<String> = FileManager("heads.yml").generateYamlFile().getStringList("easter-egg")
        private val coolingDownEggs: MutableMap<UUID, Int> = mutableMapOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<#FDDF86>Y<#FEE290>o<#FEE59A>l<#FFE8A4>k<#FFEBAE>p<#F6EFAD>l<#EEF2AB>a<#E5F6AA>i<#DCF9A8>d <#D2F9A8>Y<#C8F9A8>a<#BDF9A8>t<#B3F9A8>a<#A0F6A8>g<#8DF4A8>h<#7AF1A7>a<#67EEA7>n")
            .customEnchants("<#dcf9a8>Egg Castor")
            .lore(
                "While in range, right-click to",
                "encase up to 2 entities in eggs",
                "for a short duration, dealing",
                "damage.",
                "",
                "<red>Cooldown: 20s per egg"
            )
            .material(Material.NETHERITE_SWORD)
            .persistentData("yolkplaidyataghan")
            .vanillaEnchants(
                Enchantment.SHARPNESS to 8,
                Enchantment.UNBREAKING to 9,
                Enchantment.SMITE to 6,
                Enchantment.SWEEPING_EDGE to 5,
                Enchantment.FIRE_ASPECT to 4,
                Enchantment.MENDING to 1
            )
            .tier(Tier.EASTER_2025)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val livingEntity: LivingEntity = player.getTargetEntity(35) as? LivingEntity ?: return
        // todo: add player support
        if (livingEntity is Player || AbilityUtil.noDamagePermission(player, livingEntity)) {
            return
        }
        if (cooldownTaskPlayer(player)) {
            trapMobInEgg(livingEntity, player)
        }
    }

    override fun onEntityMove(event: EntityMoveEvent) {
        event.isCancelled = true
    }

    override fun onEntityTeleport(event: EntityTeleportEvent) {
        event.isCancelled = true
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
        livingEntity.persistentDataContainer.set(NamespacedKey(instance(), "yolkplaidyataghan"), PersistentDataType.SHORT, 1)
        livingEntity.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 240, 0, false, false, false))
        GlowManager.addToTeamForTicks(livingEntity, NamedTextColor.NAMES.values().random(), 240)
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
                    livingEntity.persistentDataContainer.remove(NamespacedKey(instance(), "yolkplaidyataghan"))
                    egg.remove()
                    this.cancel()
                }
                totalTicks+=20
            }
        }.runTaskTimer(instance(), 0L, 20L)
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
            }.runTaskLater(instance(), 400)
        }
        return true
    }


}