package dev.jsinco.luma.lumaitems.items.magical

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.UUID

class SweetBluetGemstone : CustomItem {

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()
        // Todo: why the hell do i have 3 lists for this...
        private val activeSnowballs: MutableSet<UUID> = mutableSetOf()
        private val cooldownStorm: MutableSet<UUID> = mutableSetOf()
        private val cooldownGlacier: MutableSet<UUID> = mutableSetOf()
    }
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fb9ee8&lS&#efa6eb&lw&#e3aeed&le&#d8b6f0&le&#ccbef3&lt &#c0c6f6&lB&#b4cef8&ll&#a9d6fb&lu&#9ddefe&le&#96e4fb&lt &#95e7f2&lG&#94eaea&le&#92ede1&lm&#91f0d9&ls&#90f3d0&lt&#8ff7c8&lo&#8dfabf&ln&#8cfdb7&le",
            mutableListOf("&#97E2FFTrifecta"),
            mutableListOf("Hold sneak and right-click", "to change spells.", "",
                "${AbilityType.RIGHTEOUS_DOWNFALL.friendlyName} &7-&f Launches a spell", "overhead that damages on impact", "",
                "${AbilityType.STORM.friendlyName} &7-&f Strikes a target with", "multiple lightning bolts, &c20s cooldown", "",
                "${AbilityType.GLACIER_BREAKAGE.friendlyName} &7-&f Launches a spell", "that explodes upon impact, &c1m cooldown",
                "", "&c4 lapis per spell"),
            Material.DIAMOND,
            mutableListOf("sweetbluetgemstone"),
            mutableMapOf(Enchantment.UNBREAKING to 9, Enchantment.FIRE_ASPECT to 5, Enchantment.THORNS to 4, Enchantment.SHARPNESS to 7)
        )
        //item.tier = "&#fb5a5a&lV&#fb6069&la&#fc6677&ll&#fc6c86&le&#fc7294&ln&#fd78a3&lt&#fd7eb2&li&#fb83be&ln&#f788c9&le&#f38dd4&ls &#f092df&l2&#ec97e9&l0&#e89cf4&l2&#e4a1ff&l4"
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        item.stringPersistentDatas[NamespacedKey(plugin, "ability-type")] = AbilityType.RIGHTEOUS_DOWNFALL.name
        return Pair("sweetbluetgemstone", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RIGHT_CLICK -> {
                event as PlayerInteractEvent
                val activeAbilityType: AbilityType = event.item?.itemMeta?.persistentDataContainer?.get(NamespacedKey(
                    plugin, "ability-type"), PersistentDataType.STRING)?.uppercase()?.let {
                        try {
                            AbilityType.valueOf(it)
                        } catch (e: IllegalArgumentException) {
                            AbilityType.RIGHTEOUS_DOWNFALL
                        }
                } ?: return false

                if (player.isSneaking) {
                    val meta = event.item?.itemMeta ?: return false
                    val newAbilityType = if (AbilityType.entries.indexOf(activeAbilityType) == AbilityType.entries.size - 1) {
                        AbilityType.entries[0]
                    } else {
                        AbilityType.entries[AbilityType.entries.indexOf(activeAbilityType) + 1]
                    }
                    meta.persistentDataContainer.set(NamespacedKey(plugin, "ability-type"), PersistentDataType.STRING, newAbilityType.name)
                    event.item?.itemMeta = meta

                    player.sendMessage(Util.colorcode("${Util.prefix} Changed to ${newAbilityType.friendlyName} &#E2E2E2spell"))
                } else if (player.inventory.containsAtLeast(ItemStack(Material.LAPIS_LAZULI), 4)) {

                    runAbilityType(activeAbilityType, player)
                }
            }
            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent
                val snowball = event.entity as? Snowball ?: return false

                if (snowball.hasMetadata("MAGIC_DOWNFALL")) {
                    magicalDownFallLand(snowball)
                } else if (snowball.hasMetadata("MAGIC_EXPLOSION")) {
                    magicGlacierExplosionLand(snowball)
                }
            }

            else -> return false
        }
        return true
    }

    private fun runAbilityType(abilityType: AbilityType, player: Player) {
        when (abilityType) {
            AbilityType.RIGHTEOUS_DOWNFALL -> {
                magicalDownFall(player)
            }
            AbilityType.GLACIER_BREAKAGE -> {
                magicGlacierExplosion(player)
            }
            AbilityType.STORM -> {
                val target = player.getTargetEntity(75) as? LivingEntity ?: return
                stormAbility(player, target)
            }
        }
    }


    private fun magicalDownFall(player: Player) {//todo;rem
        if (activeSnowballs.contains(player.uniqueId)) return
        val targetBlock = player.getTargetBlockExact(170) ?: return

        player.inventory.removeItem(ItemStack(Material.LAPIS_LAZULI, 4))
        val snowball = player.world.spawn(player.location.add(0.0,17.0,0.0), Snowball::class.java)
        player.hideEntity(plugin, snowball)
        for (watcher in player.getNearbyEntities(80.0, 80.0, 80.0).mapNotNull { it as? Player }) {
            watcher.hideEntity(plugin, snowball)
        }
        val vector: Vector = AbilityUtil.getDirectionBetweenLocations(snowball.location, targetBlock.location)

        snowball.velocity = vector.multiply(0.1).normalize()
        snowball.setMetadata("MAGIC_DOWNFALL", FixedMetadataValue(plugin, "MAGIC_DOWNFALL"))
        snowball.setGravity(false)
        snowball.shooter = player
        snowball.persistentDataContainer.set(NamespacedKey(plugin, "sweetbluetgemstone"), PersistentDataType.SHORT, 1)

        activeSnowballs.add(player.uniqueId)
        object : BukkitRunnable() {
            override fun run() {
                if (snowball.isDead || snowball.ticksLived > 400) {
                    this.cancel()
                    activeSnowballs.remove(player.uniqueId)
                    if (!snowball.isDead) snowball.remove()
                    return
                }
                snowball.world.playSound(snowball.location, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 1f)
                snowball.world.spawnParticle(Particle.DUST, snowball.location, 50, 0.7, 0.7, 0.7, 0.1, DustOptions(Color.WHITE, 1f))
                snowball.world.spawnParticle(Particle.WAX_OFF, snowball.location, 10, 0.7, 0.7, 0.7, 0.1)
            }
        }.runTaskTimer(plugin, 0L, 1L)
    }

    private fun magicalDownFallLand(snowball: Snowball) {
        val affected = snowball.getNearbyEntities(5.0, 5.0, 5.0).mapNotNull { it as? LivingEntity }
        val player = snowball.shooter as Player

        snowball.world.playSound(snowball.location, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.2f)
        snowball.world.spawnParticle(Particle.SOUL_FIRE_FLAME, snowball.location, 30, 0.7, 0.7, 0.7, 0.8)
        snowball.world.spawnParticle(Particle.ELECTRIC_SPARK, snowball.location, 30, 0.7, 0.7, 0.7, 0.8)
        activeSnowballs.remove(player.uniqueId)
        for (entity in affected) {
            if (!AbilityUtil.noDamagePermission(player, entity) && entity != player) {
                entity.damage(22.0, player)
            }
        }
    }


    private fun magicGlacierExplosion(player: Player) {
        if (cooldownGlacier.contains(player.uniqueId)) {
            return
        }
        player.inventory.removeItem(ItemStack(Material.LAPIS_LAZULI, 4))

        val snowball = player.launchProjectile(Snowball::class.java)
        player.hideEntity(plugin, snowball)
        for (watcher in player.getNearbyEntities(80.0, 80.0, 80.0).mapNotNull { it as? Player }) {
            watcher.hideEntity(plugin, snowball)
        }
        snowball.velocity = player.location.direction.multiply(0.16).normalize()
        snowball.setGravity(false)
        snowball.persistentDataContainer.set(NamespacedKey(plugin, "sweetbluetgemstone"), PersistentDataType.SHORT, 1)
        snowball.setMetadata("MAGIC_EXPLOSION", FixedMetadataValue(plugin, "MAGIC_EXPLOSION"))
        snowball.shooter = player

        cooldownGlacier.add(player.uniqueId)
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            cooldownGlacier.remove(player.uniqueId)
        }, AbilityType.GLACIER_BREAKAGE.cooldown)
        object : BukkitRunnable() {
            override fun run() {
                if (snowball.isDead || snowball.ticksLived > 400) {
                    this.cancel()
                    if (!snowball.isDead) snowball.remove()
                    return
                }
                snowball.world.playSound(snowball.location, Sound.BLOCK_AMETHYST_BLOCK_CHIME, 0.6f, 1f)
                snowball.world.spawnParticle(Particle.SOUL_FIRE_FLAME, snowball.location, 5, 0.3, 0.3, 0.3, 0.2)
                snowball.world.spawnParticle(Particle.GLOW, snowball.location, 5, 0.3, 0.3, 0.3, 0.2)
            }
        }.runTaskTimer(plugin, 0L, 1L)

    }

    private fun magicGlacierExplosionLand(snowball: Snowball) {
        snowball.world.playSound(snowball.location, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.2f)
        snowball.world.playSound(snowball.location, Sound.BLOCK_AMETHYST_CLUSTER_BREAK, 4f, 1f)
        snowball.world.spawnParticle(Particle.EXPLOSION, snowball.location, 1, 0.0, 0.0, 0.0, 0.0)
        snowball.world.spawnParticle(Particle.SOUL_FIRE_FLAME, snowball.location, 100, 0.5, 0.5, 0.5, 0.8)
        snowball.getNearbyEntities(10.0,10.0,10.0).mapNotNull { it as? LivingEntity }.forEach {
            if (!AbilityUtil.noDamagePermission(snowball.shooter as Player, it) && it != snowball.shooter) {
                it.damage(60.0, snowball.shooter as Player)
            }
        }
    }

    private fun stormAbility(player: Player, livingEntity: LivingEntity) {
        if (AbilityUtil.noDamagePermission(player, livingEntity) || cooldownStorm.contains(player.uniqueId)) return
        player.inventory.removeItem(ItemStack(Material.LAPIS_LAZULI, 4))

        livingEntity.fireTicks = 140
        for (i in 0..6) {
            livingEntity.world.strikeLightningEffect(livingEntity.location)
            livingEntity.damage(5.0, player)
        }

        cooldownStorm.add(player.uniqueId)
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            cooldownStorm.remove(player.uniqueId)
        }, AbilityType.STORM.cooldown)


    }




    enum class AbilityType(val friendlyName: String, val lapis: Int, val cooldown: Long) {
        RIGHTEOUS_DOWNFALL("&#E2EFFDRighteous Downfall", 5, 0),
        STORM("&#FFEB92Storm", 4, 200),
        GLACIER_BREAKAGE("&#a8c6fbG&#abc9fbl&#aecbfca&#b1cefcc&#b5d0fci&#b8d3fce&#bbd5fdr &#bed8fdB&#c1dafdr&#c4ddfee&#c7dffea&#cbe2fek&#cee4fea&#d1e7ffg&#d4e9ffe", 8, 1200);
    }
}