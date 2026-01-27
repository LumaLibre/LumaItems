package dev.lumas.lumaitems.items.tools.harrow

import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.AbilityUtil
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.QuickTasks
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import java.awt.Color
import java.util.UUID
import kotlin.random.Random
import org.bukkit.Color as BukkitColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.type.Sapling
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.persistence.PersistentDataType


class TahluraHoeItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("tahlura-hoe")
        private const val ACTIVATOR = "activator"
        private val REFERENCES: MutableMap<UUID, RainCloud> = mutableMapOf()
        private val FORTUNE_8_HOE = ItemStack(Material.NETHERITE_HOE).apply {
            addUnsafeEnchantment(Enchantment.FORTUNE, 8)
        }
        private val AIR = ItemStack(Material.AIR)
        private val WIND_CHARGE = ItemStack(Material.WIND_CHARGE)
        private val RAIN_PARTICLE = ParticleDisplay.of(Particle.RAIN)
        private val DARK_GRAY_DUST = ParticleDisplay.of(Particle.DUST).withColor(Color.DARK_GRAY)
        private val COLORS = listOf("#da8e5e", "#b4ad7b", "#E9D9C2", "#dfbc9a", "#b09886").map { Color.decode(it) }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#da8e5e:#b4ad7b:#E9D9C2:#dfbc9a:#b09886>Tahlura Hoe</gradient></b>")
            .customEnchants("<#b4ad7b>Stormroot")
            .persistentData(KEY)
            .material(Material.NETHERITE_HOE)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(
                Enchantment.FORTUNE to 5,
                Enchantment.UNBREAKING to 9,
                Enchantment.MENDING to 1,
                Enchantment.EFFICIENCY to 7
            )
            .lore(
                "<#b4ad7b>Right-click</#b4ad7b> to summon a",
                "thunderstorm cloud that",
                "rains a special kind of",
                "growing elixir.",
                "",
                "Crops and saplings that",
                "are rained on will grow",
                "instantly. Crops struck",
                "by lightning will drop",
                "extra produce.",
                "",
                "<red>Cooldown: 3m"
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) return
        QuickTasks.addCooldown(this, player.uniqueId, 3600)
        this.rainCloudSeed(player)
        player.swingMainHand()
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return

        if (snowball.hasMetadata(ACTIVATOR)) {
            val rainCloud = RainCloud(player, snowball.location)
            rainCloud.seed {
                rainCloud.spawnCloud(300)
            }
            snowball.world.playSound(snowball.location, Sound.ENTITY_EVOKER_PREPARE_SUMMON, 0.3f, 0.8f)
            return
        }

        RAIN_PARTICLE.spawn(snowball.location)

        // 10% chance
        val chance = random().nextInt(1, 11)
        val raincloud = REFERENCES[player.uniqueId]

        if (event.hitEntity != null) {
            event.isCancelled = true
        }

        if (chance == 1) {
            raincloud?.lightningStrike(snowball.location)
            raincloud?.breakGrownCrops(snowball.location)
        }

        raincloud?.growAgeableBlocks(snowball.location)
    }

    private fun rainCloudSeed(player: Player) {
        val snowball = player.launchProjectile(Snowball::class.java)
        snowball.setMetadata(ACTIVATOR, FixedMetadataValue(LumaItems.getInstance(), true))
        Util.setPersistentKey(snowball, KEY, PersistentDataType.SHORT, 1)
        snowball.velocity = snowball.velocity.multiply(0.35)
        snowball.isPersistent = false
        snowball.item = WIND_CHARGE

        Executors.asyncTimer(0, 1) { task ->
            if (!snowball.isValid) {
                task.cancel()
                return@asyncTimer
            }
            DARK_GRAY_DUST.spawn(snowball.location)
        }
    }

    private class RainCloud(
        val shooter: Player,
        val spawnLoc: Location
    ) {

        companion object {
            val GRAY_TRANSITION_DUST = ParticleDisplay.of(Particle.DUST_COLOR_TRANSITION)
                .withTransitionColor(Color.DARK_GRAY, 2f, Color.WHITE)
            val BLUE_DUST = ParticleDisplay.of(Particle.DUST_COLOR_TRANSITION)
                .withTransitionColor(Color.decode("#AEC6CF"), 1.2f, Color.BLUE)
        }

        val cloudSpawnLoc = spawnLoc.clone().add(0.0, 11.0, 0.0)


        fun seed(whenDone: () -> Unit) {
            val lineCurrentEnd: Location = spawnLoc.clone()

            Executors.asyncTimer(0, 1) { task ->
                val toDestination = cloudSpawnLoc.clone().subtract(lineCurrentEnd)
                val distance = toDestination.length()

                if (distance < 1) {
                    task.cancel()
                    whenDone()
                    return@asyncTimer
                }

                val directionStep = toDestination.toVector().normalize().multiply(0.9)
                lineCurrentEnd.add(directionStep)
                Particles.line(spawnLoc, lineCurrentEnd, 0.35, GRAY_TRANSITION_DUST)
            }
        }

        fun spawnCloud(ticks: Int) {
            REFERENCES[shooter.uniqueId] = this
            val snowballs: MutableList<Snowball> = mutableListOf()
            val position = cloudSpawnLoc.clone()
            var count = 0

            Executors.asyncTimer(0, 1) { task ->
                if (++count > ticks) {
                    task.cancel()
                    REFERENCES.remove(shooter.uniqueId)
                    return@asyncTimer
                }

                snowballs.removeIf { !it.isValid }

                val baseDir = shooter.eyeLocation.direction.clone().normalize()

                val pushVector = baseDir.multiply(0.075).apply {
                    y = 0.0
                }

                position.add(pushVector)

                position.world.spawnParticle(Particle.DUST_COLOR_TRANSITION, position, 150, 3.0, 0.5, 3.0,
                    Particle.DustTransition(BukkitColor.GRAY, BukkitColor.WHITE, 2f))


                val randLoc = position.clone().add(Random.nextDouble(-6.0, 6.0), 0.0, Random.nextDouble(-6.0, 6.0))
                RAIN_PARTICLE.spawn(randLoc)

                snowballs.forEach {
                    BLUE_DUST.spawn(it.location)
                }

                Executors.sync {
                    snowball(randLoc).also {
                        snowballs.add(it)
                    }
                }

            }
        }


        fun lightningStrike(location: Location) {
            val groundY = location.y
            val cloudY = cloudSpawnLoc.y
            val heightDifference = (cloudY - groundY).coerceAtLeast(0.0)

            Executors.async {
                val lightningLength = heightDifference * 0.042

                Particles.lightning(
                    location, BukkitVectors.UP,
                    20, 200, 0.5,
                    2.0, 1.0, lightningLength,
                    1.0, 0.1, 0.8,
                    DARK_GRAY_DUST.clone().withColor(COLORS.random())
                )
            }

            // Synchronous stuff
            location.getNearbyLivingEntities(0.5, heightDifference, 0.5).forEach { livingEntity ->
                if (AbilityUtil.noDamagePermission(shooter, livingEntity)) {
                    return@forEach
                }

                livingEntity.damage(5.0, shooter)
            }

            location.world.playSound(location, Sound.ENTITY_LIGHTNING_BOLT_THUNDER, 0.04f, 2.0f)
        }

        fun growAgeableBlocks(loc: Location, radius: Double = 2.0) {

            val sphere = Sphere(loc, radius)
            sphere.getSphereFast { block ->
                when (val blockData: BlockData = block.blockData) {
                    is Ageable -> {
                        if (AbilityUtil.noBuildPermission(shooter, loc.block)) {
                            return@getSphereFast
                        }

                        if (blockData.material == Material.SUGAR_CANE) {
                            block.randomTick()
                        } else if (blockData.age < blockData.maximumAge) {
                            blockData.age++
                            block.blockData = blockData
                            block.world.spawnParticle(Particle.HAPPY_VILLAGER, block.location.toCenterLocation(), 5, 0.3, 0.3, 0.3, 0.0)
                        }
                    }
                    is Sapling -> {
                        if (AbilityUtil.noBuildPermission(shooter, loc.block)) {
                            return@getSphereFast
                        }

                        if (blockData.stage < blockData.maximumStage) {
                            blockData.stage++
                        }
                        block.blockData = blockData
                        block.randomTick()
                        block.world.spawnParticle(Particle.HAPPY_VILLAGER, block.location.toCenterLocation(), 5, 0.3, 0.3, 0.3, 0.0)
                    }
                    else -> return@getSphereFast
                }
            }

        }

        fun breakGrownCrops(loc: Location, radius: Double = 1.0) {
            val sphere = Sphere(loc, radius)
            sphere.getSphereFast { block ->
                val blockData: BlockData = block.blockData
                if (blockData is Ageable && blockData.age == blockData.maximumAge) {
                    block.getDrops(FORTUNE_8_HOE).forEach { itemStack ->
                        block.world.dropItemNaturally(block.location.toCenterLocation(), itemStack)
                    }
                    blockData.age = 0
                    block.blockData = blockData
                }
            }
        }


        private fun snowball(loc: Location): Snowball {
            val snowball = loc.world.createEntity(loc, Snowball::class.java)
            snowball.shooter = shooter
            snowball.isSilent = true
            snowball.isPersistent = false
            snowball.item = AIR
            Util.setPersistentKey(snowball, KEY, PersistentDataType.SHORT, 1)

            snowball.spawnAt(loc)
            snowball.location.getNearbyPlayers(90.0).forEach {
                it.hideEntity(LumaItems.getInstance(), snowball)
            }
            return snowball
        }
    }
}