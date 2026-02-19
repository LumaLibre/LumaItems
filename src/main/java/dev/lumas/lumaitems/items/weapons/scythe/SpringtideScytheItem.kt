package dev.lumas.lumaitems.items.weapons.scythe

import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.tiers.Tier
import java.util.concurrent.TimeUnit
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

@Disable(WorldName.PINATA)
class SpringtideScytheItem : CustomItemFunctions() {

    companion object {
        private const val CIRCLE_RADIUS = 3.0
        private val ALT_PARTICLE_COLORS = listOf(
            "#CC96FF",
            "#E88CA6",
            "#B8EF9E",
        ).map { Util.hex2AwtColor(it) }
        private val NETHER_STAR = ItemStack(Material.NETHER_STAR)
        private val WHITE_DUST = DustOptions(Color.WHITE, 1f)
        private val persistentKey = Util.namespacedKey("wooly-scythe")
        private val DOWNPOUR_SLOWNESS = PotionEffect(PotionEffectType.SLOWNESS, 150, 2, false, false, false)
    }



    private val fallingProjectile: (loc: Location, shooter: Player, spellType: SpellType) -> Snowball = fun(loc: Location, shooter: Player, spellType: SpellType): Snowball {
        val projectile = loc.world.createEntity(loc, Snowball::class.java)
        projectile.shooter = shooter
        projectile.item = NETHER_STAR
        Util.setPersistentKey(projectile, persistentKey, PersistentDataType.STRING, spellType.name)
        return projectile
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#CC96FF:#E88CA6:#B8EF9E>Springtide Scythe</gradient></b>")
            .persistentData(persistentKey)
            .material(Material.NETHERITE_HOE)
            .tier(Tier.EASTER_2025)
            .customEnchants(
                "<#CC96FF>Starcrossers",
                "<#E88CA6>Downpour"
            )
            .vanillaEnchants(
                Enchantment.SHARPNESS to 10,
                Enchantment.BANE_OF_ARTHROPODS to 8,
                Enchantment.SMITE to 8,
                Enchantment.LOOTING to 6,
                Enchantment.UNBREAKING to 5,
                Enchantment.MENDING to 1
            )
            .lore(
                "<#CC96FF>Starcrossers (Night)</#CC96FF> <gray>-</gray> Right-",
                "click to launch <#CC96FF>2</#CC96FF> falling stars",
                "that deal damage on impact.",
                "",
                "<#E88CA6>Downpour (Day)</#E88CA6> <gray>-</gray> Right-click",
                "to summon a rainfall of stars",
                "that deal damage to hit",
                "entities.",
                "",
                "<red>Cooldown: 8s"
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            return
        }
        QuickTasks.addCooldown(this, player.uniqueId, 160L)


        if (player.world.isDayTime) {
            val targetLocation =
                (player.getTargetEntity(45) as? LivingEntity)?.location
                    ?: player.getTargetBlock(null, 45).location.add(0.5, 1.0, 0.5)
            this.downPour(targetLocation, player)
        } else {
            val target = player.getTargetEntity(45) ?: player.getTargetBlock(null, 45).location
            this.righteousStarCrossers(target, player)
        }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity
        val spellType = SpellType.getSpellType(snowball) ?: return

        if (spellType == SpellType.RIGHTEOUS_STARCROSSERS) {
            snowball.world.playSound(snowball.location, Sound.ENTITY_GENERIC_EXPLODE, 2f, 1.2f)
            snowball.world.spawnParticle(Particle.SOUL_FIRE_FLAME, snowball.location, 30, 0.7, 0.7, 0.7, 0.8)
            snowball.world.spawnParticle(Particle.ELECTRIC_SPARK, snowball.location, 30, 0.7, 0.7, 0.7, 0.8)

            val hitLocation = snowball.location
            var damage = 25.0
            hitLocation.getNearbyLivingEntities(3.0).forEach {
                if (AbilityUtil.noDamagePermission(player, it) || it == player) {
                    return
                }
                it.damage(damage, player)
                damage -= 1
            }

        } else {
            event.entity.world.playSound(event.entity.location, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 1f)
            var damage = 20.0
            val hitEntity = event.hitEntity as? LivingEntity ?: run {
                val hitBlock = event.hitBlock ?: return
                return@run hitBlock.location.getNearbyLivingEntities(0.8).firstOrNull() ?: return
            }.also { damage /= 2 }
            if (!AbilityUtil.noDamagePermission(player, hitEntity) && hitEntity != player) {
                hitEntity.damage(damage, player)
            }
        }
    }


    private fun righteousStarCrossers(entityOrLocation: Any, player: Player) {
        val snowballs: MutableList<Snowball> = mutableListOf()
        val radius = 8.0
        var ticksRan = 0

        for (i in 0 until 2) {
            val spawnLocation = player.location.clone().add(
                random().nextDouble(-radius, radius),
                10.0,
                random().nextDouble(-radius, radius)
            )
            val snowball = fallingProjectile(spawnLocation, player, SpellType.RIGHTEOUS_STARCROSSERS)
            snowballs.add(snowball)
            player.world.addEntity(snowball)
        }

        Executors.asyncTimer(0, 1) { task ->
            if (snowballs.isEmpty() || ticksRan++ > 80) {
                task.cancel()
                return@asyncTimer
            }
            snowballs.removeIf {
                if (it.isDead) {
                    return@removeIf true
                }
                val loc = if (entityOrLocation is Entity) {
                    if (snowballs.size > 1)  {
                        entityOrLocation.boundingBox.center.toLocation(player.world)
                    } else {
                        task.cancel()
                        return@removeIf true
                    }
                } else {
                    entityOrLocation as Location
                }

                it.velocity = BukkitVectors.direction(it.location, loc).multiply(0.1).normalize()
                it.world.spawnParticle(Particle.DUST, it.location, 50, 0.7, 0.7, 0.7, 0.1, WHITE_DUST)
                it.world.spawnParticle(Particle.WAX_OFF, it.location, 10, 0.7, 0.7, 0.7, 0.1)

                it.sync {
                    it.world.playSound(it.location, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, 1f)
                }
                return@removeIf false
            }
        }
    }

    private fun downPour(location: Location, player: Player) {
        var ticksRan = 0
        val snowballs: MutableList<Projectile> = mutableListOf()
        val display = ParticleDisplay.of(Particle.DUST)
            .withLocation(location)
            .withColor(ALT_PARTICLE_COLORS.random())

        location.getNearbyLivingEntities(CIRCLE_RADIUS).forEach {
            if (it != player) {
                it.addPotionEffect(DOWNPOUR_SLOWNESS)
            }
        }


        Bukkit.getAsyncScheduler().runAtFixedRate(instance(), { task ->
            if (ticksRan++ > 150) {
                task.cancel()
                return@runAtFixedRate
            }
            Particles.neopaganPentagram(CIRCLE_RADIUS, 0.05, 0.0, display, display)
            snowballs.removeIf { projectile ->
                if (!projectile.isDead) {
                    projectile.world.spawnParticle(Particle.DUST, projectile.location, 1, WHITE_DUST)
                    return@removeIf false
                }
                return@removeIf true
            }

            val spawnLocation = location.clone().toCenterLocation().add(
                random().nextDouble(-CIRCLE_RADIUS, CIRCLE_RADIUS),
                random().nextInt(9, 12).toDouble(),
                random().nextDouble(-CIRCLE_RADIUS, CIRCLE_RADIUS)
            )
            val projectile = fallingProjectile(spawnLocation, player, SpellType.NIGHTFALL_DOWNPOUR)
            snowballs.add(projectile)
            location.sync {
                location.world.addEntity(projectile)
            }
        }, 0, 50, TimeUnit.MILLISECONDS)
    }

    enum class SpellType {
        RIGHTEOUS_STARCROSSERS,
        NIGHTFALL_DOWNPOUR;

        companion object {
            fun getSpellType(persistentDataHolder: PersistentDataHolder): SpellType? {
                val value = Util.getPersistentKey(persistentDataHolder, persistentKey, PersistentDataType.STRING)
                    ?: return null
                return Util.enumValueOfOrNull(SpellType::class.java, value)
            }
        }
    }
}