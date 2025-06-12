package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.QuickTasks
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Color as BukkitColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Enemy
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.entity.Projectile
import org.bukkit.entity.Snowball
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class StormSurgeSpadeItem : CustomItemFunctions() {

    companion object {
        private val ITEM_STACK = ItemStack(Material.WIND_CHARGE)
        private val WHITE_DUST = Particle.DustOptions(BukkitColor.WHITE, 1f)
        private val key = Util.namespacedKey("storm-surge-spade")
    }

    private val fallingProjectile = fun(loc: Location, shooter: Player): Snowball {
        val projectile = loc.world.spawn(loc, Snowball::class.java)
        projectile.shooter = shooter
        projectile.item = ITEM_STACK
        Util.setPersistentKey(projectile, key, PersistentDataType.SHORT, 0)
        return projectile
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#679AA5:#70A9AC:#6e8c96:#45566a>Storm Surge Spade</gradient></b>")
            .customEnchants("<#679AA5>Hailing Geyser")
            .material(Material.NETHERITE_SHOVEL)
            .persistentData(key)
            .tier(Tier.DEBUG)
            .lore(
                "<#679AA5>Right-click</#679AA5> to summon a storm cloud",
                "that rains down a torrent of hail.",
                "",
                "Hail from the storm will destroy",
                "blocks in its path and propel items",
                "towards you.",
                "",
                "<red>Cooldown: 3m"
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 7,
                Enchantment.UNBREAKING to 7,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId) || player.location.block.lightFromSky < 1) {
            return
        }
        QuickTasks.addCooldown(this, player.uniqueId, 3600) // 3 min
        starfallRaincloud(player)
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val hitEntity = event.hitEntity
        if (hitEntity == player) {
            event.isCancelled = true
        } else if (hitEntity is Enemy) {
            hitEntity.damage(5.0, player)
        }

        val block = event.hitBlock ?: return
        val item = player.inventory.itemInMainHand
        if (!block.isPreferredTool(item)) {
            return // Only apply if the block is a preferred tool
        }
        //player.breakBlock(block)
        block.breakNaturally(item)
        Executors.syncDelayed(1) {
            block.location.getNearbyEntitiesByType(Item::class.java, 1.5, 1.0, 1.5)
                .forEach {
                    propelToPlayer(player, it)
                }
        }
    }

    private fun starfallRaincloud(player: Player) {
        val loc = player.location.clone()
        val spawnLocation = loc.add(0.0, 10.0, 0.0)
        val direction = loc.direction.clone().normalize()


        val snowballs = mutableListOf<Projectile>()
        var count = 0
        Executors.asyncTimer(0, 1) { task ->
            if (count++ > 200) {
                task.cancel()
                return@asyncTimer
            }

            snowballs.removeIf { it.isDead }
            // Move cloud 0.1 blocks per tick in horizontal direction
            val stormVector = direction.clone()
            val yFactor = stormVector.y / 2
            stormVector.x += yFactor
            stormVector.z += yFactor
            stormVector.setY(0).multiply(0.1)
            spawnLocation.add(stormVector)


            player.world.spawnParticle(
                Particle.CLOUD,
                spawnLocation,
                60,
                3.0, 0.0, 3.0,
                0.0
            )

            val randLoc = spawnLocation.clone().add(random().nextDouble(-6.0, 6.0), 0.0, random().nextDouble(-6.0, 6.0))
            Executors.sync {
                val snowball = fallingProjectile(randLoc, player)
                //snowball.velocity = direction.clone().multiply(0.2)
                // go in the player's facing direction
                snowballs.add(snowball)
            }

            snowballs.forEach { projectile ->
                projectile.world.spawnParticle(Particle.DUST, projectile.location, 1, WHITE_DUST)
            }


        }
    }

    private fun propelToPlayer(player: Player, item: Item) {
        val target = player.eyeLocation.clone().add(0.0, 0.3, 0.0)
        val origin = item.location

        val distance = target.distance(origin)

        if (distance < 1.0 && distance > -1.0) {
            return // don't apply if the item is too close
        }

        val direction = target.toVector().subtract(origin.toVector()).normalize()
        // if the items location is already above the player's head dont apply any Y
        if (origin.y > target.y) {
            direction.setY(0.0)
        } else {
            direction.setY(0.5 + (distance * 0.1))
        }

        val speed = 0.5
        item.velocity = direction.multiply(speed)
    }
}