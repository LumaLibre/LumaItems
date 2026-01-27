package dev.lumas.lumaitems.items.armor.chestplate

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.manager.FileManager
import dev.lumas.lumaitems.manager.GlowManager
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.Util
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.Cancellable
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Transformation
import org.joml.Vector3f
import kotlin.random.Random

class YolkplaidYarweaveItem : CustomItem {

    companion object {
        private val eggTextures: List<String> = FileManager("heads.yml").generateYamlFile().getStringList("easter-egg")
        private val SLOWNESS = PotionEffect(PotionEffectType.SLOWNESS, 240, 200, false, false, false)
    }


    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#FADC84&lY&#F5DF88&lo&#EFE38D&ll&#EAE691&lk&#E5E995&lp&#DFEC99&ll&#DAF09E&la&#D4F3A2&li&#CFF6A6&ld &#C2F5A6&lY&#B5F3A6&la&#A8F2A6&lr&#9BF1A6&lw&#8DEFA5&le&#80EEA5&la&#73ECA5&lv&#66EBA5&le",
            mutableListOf("&#dcf9a8Egg Castor", "&#dcf9a8Shelling"),
            mutableListOf("While worn, attacking enemies may be", "encased in eggs, dealing damage.", "", "When being attacked, attackers will", "passively be inflicted damage by", "egg shellings."),
            Material.NETHERITE_CHESTPLATE,
            mutableListOf("yolkplaidyarweave"),
            mutableMapOf(Enchantment.PROTECTION to 7, Enchantment.UNBREAKING to 8, Enchantment.BLAST_PROTECTION to 6, Enchantment.MENDING to 1)
        )
        item.addQuote("&#CFF6A6\"&#D0F5A5S&#D2F4A4m&#D3F4A3e&#D4F3A2a&#D6F2A1r&#D7F1A0e&#D8F09Fd &#DAF09Ew&#DBEF9Ci&#DCEE9Bt&#DEED9Ah &#DFEC99y&#E0EB98o&#E2EB97l&#E3EA96k&#E5E995s&#E6E894; &#E7E793i&#E9E792t &#EAE691s&#EBE590m&#EDE48Fe&#EEE38El&#EFE38Dl&#F1E28Bs &#F2E18Aa&#F3E089w&#F5DF88f&#F6DE87u&#F7DE86l&#F9DD85.&#FADC84\"")
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"

        return Pair("yolkplaidyarweave", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.PLAYER_DAMAGED_BY_ENTITY -> {
                event as EntityDamageByEntityEvent

                if (!Util.isItemInSlot("yolkplaidyarweave", EquipmentSlot.CHEST, player)) {
                    return false
                }

                val damager = event.damager as? LivingEntity ?: return false

                if (Random.nextInt(10) == 5 && damager !is Player) {
                    encase(damager, player)
                } else {
                    shellings(damager, player)
                }

            }
            Action.ENTITY_TELEPORT -> {
                event as Cancellable
                event.isCancelled = true
            }
            else -> return false
        }
        return true
    }

    private fun shellings(damager: LivingEntity, player: Player) {

        val loc = player.boundingBox.center.toLocation(player.world)
        val locDamager = damager.boundingBox.center.toLocation(damager.world)

        player.world.spawnParticle(Particle.ITEM, loc, 20, 0.5, 0.5, 0.5, 0.1, ItemStack(Material.EGG))
        damager.world.spawnParticle(Particle.BLOCK, locDamager, 20, 0.5, 0.5, 0.5, 0.1, Material.TURTLE_EGG.createBlockData())
        player.world.playSound(loc, Sound.ENTITY_TURTLE_EGG_CRACK, 1f, 1f)

        damager.damage(Random.nextDouble(2.0, 4.0))
    }

    private fun encase(livingEntity: LivingEntity, attacker: Player) {
        if (AbilityUtil.isMythicMob(livingEntity)) return

        val loc = livingEntity.eyeLocation.add(0.0, 0.5, 0.0); loc.yaw = 0.0f; loc.pitch = 0.0f
        val egg = livingEntity.world.spawnEntity(loc, EntityType.ITEM_DISPLAY) as ItemDisplay
        egg.isPersistent = false
        egg.setItemStack(Util.playerHeadFromBase64(eggTextures.random(), 1))
        egg.interpolationDuration = 0
        egg.interpolationDelay = -1


        val boundingBox = livingEntity.boundingBox

        val transformation: Transformation = egg.transformation
        transformation.scale.set(Vector3f(boundingBox.widthX.toFloat(), boundingBox.height.toFloat(), boundingBox.widthZ.toFloat()).mul(3.0f))
        egg.transformation = transformation



        livingEntity.isCollidable = false
        livingEntity.addPotionEffect(SLOWNESS)
        livingEntity.addPotionEffect(PotionEffect(PotionEffectType.GLOWING, 240, 0, false, false, false))
        GlowManager.addToTeamForTicks(livingEntity, ChatColor.RED, 240)
        object: BukkitRunnable() {
            var totalTicks = 0

            override fun run() {
                var stop = false
                if (totalTicks >= 240) {
                    livingEntity.isCollidable = true
                    stop = true
                } else if (livingEntity.isDead) {
                    stop = true
                }

                livingEntity.damage(4.0, attacker)
                livingEntity.world.playSound(livingEntity.location, Sound.ENTITY_PLAYER_ATTACK_CRIT, 2f, 7.7f)

                if (stop) {
                    egg.remove()
                    this.cancel()
                }
                totalTicks+=20
            }
        }.runTaskTimer(instance(), 0L, 20L)
    }
}