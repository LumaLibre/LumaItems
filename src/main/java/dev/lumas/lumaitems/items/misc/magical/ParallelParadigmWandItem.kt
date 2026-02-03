package dev.lumas.lumaitems.items.misc.magical

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.Executors.syncEntityDelayed
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.TNTPrimed
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.random.Random

class ParallelParadigmWandItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#349532&lP&#499433&la&#5e9333&lr&#739234&la&#899034&ll&#9e8f35&ll&#b38e35&le&#c88d36&ll &#dd8c36&lP&#f28b37&la&#fb8742&lr&#f68057&la&#f17a6c&ld&#ed7481&li&#e86d96&lg&#e467ab&lm &#df60c0&lW&#da5ad5&la&#d653ea&ln&#d14dff&ld",
            mutableListOf("&#49b12fP&#5ca34da&#70946ar&#838688a&#9778a6d&#aa6ac4i&#be5be1g&#d14dffm"),
            mutableListOf("&#349532\"&#3e913fT&#488c4ch&#518858i&#5b8365n&#657f72g&#6f7a7fs &#79768ca&#837199r&#8c6da5e&#9668b2n&#a064bf'&#aa5fcct &#b45bd9a&#bd56e5l&#c752f2w&#d14dffa&#d251f2y&#d355e6s &#d359d9a&#d45dcds &#d561c0t&#d665b4h&#d669a7e&#d76d9by &#d8708es&#d97481e&#d97875e&#da7c68m&#db805c.&#dc844f.&#dc8843.&#dd8c36\"","","Left-click to cast a spell", "", "Spells from this wand may", "vary, caution is advised!", "", "&c1 Lapis per spell"),
            Material.BLAZE_ROD,
            mutableListOf("parallelparadigmwand"),
            mutableMapOf(Enchantment.SHARPNESS to 5, Enchantment.BANE_OF_ARTHROPODS to 5, Enchantment.FIRE_ASPECT to 4)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("parallelparadigmwand", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.LEFT_CLICK -> {
                AbilityUtil.spawnSpell(player, Particle.FIREWORK, "parallelparadigmwand", 120L)
            }

            Action.PROJECTILE_LAND -> {
                event as ProjectileHitEvent
                val entity = event.hitEntity ?: return false
                if (entity is LivingEntity && entity.type != EntityType.ARMOR_STAND && !AbilityUtil.noDamagePermission(player, entity)) {
                    if (!takeLapisCost(player)) return false
                    player.playSound(player.location, Sound.ENTITY_EVOKER_CAST_SPELL, 1f, 1f)

                    when (Random.nextInt(1, 6)) {
                        1 -> ingniteEntity(entity)
                        2 -> potionEntity(entity)
                        3 -> tntEntity(entity)
                        4 -> swapEntityLocations(entity, player)
                        5 -> expEntity(entity)
                    }
                }
            }
            else -> return false
        }
        return true
    }

    private fun takeLapisCost(player: Player): Boolean {
        if (player.inventory.contains(Material.LAPIS_LAZULI, 1)) {
            player.inventory.removeItem(ItemStack(Material.LAPIS_LAZULI, 1))
            return true
        }
        return false
    }


    private fun ingniteEntity(entity: LivingEntity) {
        for (i in 0..60) {
            entity.world.spawnParticle(Particle.FLAME, entity.location, 1, 0.3, 0.3, 0.3, 0.2)
        }
        entity.world.playSound(entity.location, Sound.ENTITY_BLAZE_SHOOT, 1f, 0.9f)
        entity.fireTicks = 100
    }

    private fun potionEntity(entity: LivingEntity) {
        for (i in 0..60) {
            entity.world.spawnParticle(Particle.WITCH, entity.location, 1, 0.3, 0.3, 0.3, 0.2)
        }
        entity.world.playSound(entity.location, Sound.ITEM_BOTTLE_FILL, 1f, 0.9f)
        entity.addPotionEffect(PotionEffect(PotionEffectType.values().random(), 100, 1, false, true, false))
    }

    private fun tntEntity(entity: LivingEntity) { // TODO: fake tnt?
        val tnts = mutableListOf<TNTPrimed>()
        for (i in 0..3) {
            val random = Random.nextDouble(1.0)
            val random2 = Random.nextDouble(1.0)
            val tnt = entity.location.world.spawn(entity.location.add(random,2.0,random2), TNTPrimed::class.java)
            tnt.fuseTicks = 20
            tnts.add(tnt)
        }
        entity.world.playSound(entity.location, Sound.ENTITY_TNT_PRIMED, 1f, 0.9f)

        entity.syncEntityDelayed(19) {
            entity.damage(30.0)
            entity.world.createExplosion(entity.location, 3f, false, false)
            for (tnt in tnts) {
                tnt.remove()
            }
        }
    }

    private fun swapEntityLocations(entity: LivingEntity, entity2: LivingEntity) {
        val loc1 = entity.location
        val loc2 = entity2.location
        entity.teleport(loc2)
        entity2.teleport(loc1)
        entity.world.playSound(entity.location, Sound.ENTITY_WITCH_CELEBRATE, 1f, 0.9f)
        entity2.world.playSound(entity2.location, Sound.ENTITY_WITCH_CELEBRATE, 1f, 0.9f)
    }

    private fun expEntity(entity: Entity) {
        for (i in 0..3) {
            val random = Random.nextDouble(1.0)
            val random2 = Random.nextDouble(1.0)
            entity.world.spawnEntity(entity.location.add(random,3.0,random2), EntityType.EXPERIENCE_BOTTLE)
        }
    }
}

/*private fun spawnWandProjectile(player: Player) {
    val snowball = player.launchProjectile(Snowball::class.java)
    snowball.setGravity(false)
    snowball.velocity = player.location.direction.multiply(3)
    snowball.persistentDataContainer.set(NamespacedKey(plugin, "parallelparadigmwand"), PersistentDataType.SHORT, 1)
    player.hideEntity(plugin, snowball)
    for (entity in player.getNearbyEntities(65.0, 65.0, 65.0)) {
        if (entity is Player) {
            entity.hideEntity(plugin, snowball)
        }
    }

    object : BukkitRunnable() {
        override fun run() {
            if (snowball.isDead) {
                cancel()
            }
            snowball.world.spawnParticle(Particle.FIREWORKS_SPARK, snowball.location, 4, 0.1, 0.1, 0.1, 0.0)

        }
    }.runTaskTimer(plugin, 0, 1)
    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
        if (!snowball.isDead) {
            snowball.remove()
        }
    }, 120L)
}*/