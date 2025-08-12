package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.enums.BlockConstants
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.particles.ParticleDisplay
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.BukkitVectors
import dev.jsinco.luma.lumaitems.util.Executors
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import java.awt.Color
import kotlin.math.min
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.entity.Snowball
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector

class BlobbleHammerItem : CustomItemFunctions() {

    companion object {
        private val nameSpace = Util.namespacedKey("blobble-hammer")

        private val COLORS: Map<Material, Pair<ItemStack, ParticleDisplay>> = mapOf(
            Material.DIAMOND to Pair(Material.LIGHT_BLUE_DYE, "#A2BFFE"),
            Material.LAPIS_LAZULI to Pair(Material.BLUE_DYE, "#779ECB"),
            Material.EMERALD to Pair(Material.GREEN_DYE, "#D1FEB8"),
            Material.RAW_GOLD to Pair(Material.YELLOW_DYE, "#FFEE8C"),
            Material.RAW_COPPER to Pair(Material.ORANGE_DYE, "#FFC067"),
            Material.RAW_IRON to Pair(Material.LIGHT_GRAY_DYE, "#DDD3D2"),
            Material.COAL to Pair(Material.BLACK_DYE, "#1D1C1A"),
            Material.REDSTONE to Pair(Material.RED_DYE, "#FF746C"),
            Material.QUARTZ to Pair(Material.WHITE_DYE, "#FAF8F6"),
            Material.ANCIENT_DEBRIS to Pair(Material.BROWN_DYE, "#836953"),
            Material.GOLD_NUGGET to Pair(Material.YELLOW_DYE, "#FFEE8C")
        ).mapValues { ItemStack(it.value.first).apply { addUnsafeEnchantment(Enchantment.UNBREAKING, 1) } to ParticleDisplay.of(Particle.DUST).withColor(Color.decode(it.value.second)) }

        private val HOLDER_PICKAXE_ITEM = ItemStack(Material.NETHERITE_PICKAXE).apply {
            addUnsafeEnchantment(Enchantment.FORTUNE, 5)
        }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#A2BFFE:#D1FEB8:#FFEE8C:#FFC067:#FF746C>Blobble Hammer</gradient></b>")
            .customEnchants("<#FFEE8C>Bubble Breaker")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData(nameSpace)
            .tier(Tier.SUMMER_2025)
            .quotes("<gradient:#A2BFFE:#D1FEB8:#FFEE8C:#FFC067:#FF746C>\"What the h*ll is a blobble?!\"</gradient>")
            .lore(
                "The latest sensation of",
                "the bubble revolution, the",
                "blobbles.",
                "",
                "<#FFEE8C>When breaking ores</#FFEE8C>, bubbles",
                "will lob outwards, destroying",
                "any touched blocks."
            )
            .vanillaEnchants(
                Enchantment.FORTUNE to 5,
                Enchantment.UNBREAKING to 8,
                Enchantment.MENDING to 1,
                Enchantment.EFFICIENCY to 7
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (!BlockConstants.ORES.contains(block.type)) {
            return
        }
        block.getDrops(HOLDER_PICKAXE_ITEM).ifEmpty { return }
            .forEach { drop ->
                val amt = min(drop.amount / 2, 5)
                bubble(block.location.toCenterLocation(), player, drop.type, amt)
            }
    }

    override fun onProjectileLand(player: Player, event: ProjectileHitEvent) {
        val snowball = event.entity as? Snowball ?: return

        val hitBlockFace = event.hitBlockFace ?: return

        event.hitBlock?.let { hitBlock ->
            if (hitBlock.isPreferredTool(HOLDER_PICKAXE_ITEM) && !BlockConstants.BLACKLISTED.contains(hitBlock.type) && !AbilityUtil.noBuildPermission(player, hitBlock)) {
                val item = player.inventory.itemInMainHand
                item.damage(1, player) // no direct reference for item damaging.
                hitBlock.breakNaturally(HOLDER_PICKAXE_ITEM, true, true)
            }
        }


        if (snowball.location.distance(player.location) > 20 || snowball.ticksLived >= 30) {
            snowball.remove()
            return
        }

        val vector = BukkitVectors.bounceWithBlockFace(snowball, hitBlockFace, 1.0)
        cloneSnowball(snowball, vector)
        snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.0f, 1.0f)
    }

    private fun bubble(origin: Location, player: Player, type: Material, amt: Int = 1) {
        val snowballs = mutableListOf<Snowball>()
        for (i in 0 until amt) {
            val snowball = player.world.createEntity(origin, Snowball::class.java)
            snowball.shooter = player
            snowball.isPersistent = false
            COLORS[type]?.first?.let { snowball.item = it }

            // launch in random direction away from origin point with a slight upward angle
            val dir = Vector(random().nextDouble(-1.0, 1.0), 0.0, random().nextDouble(-1.0, 1.0))
                .normalize()

            val vector = dir.multiply(0.1).setY(0.2) // speed + upward push
            snowball.velocity = vector
            Util.setPersistentKey(snowball, nameSpace, PersistentDataType.SHORT, 1)
            snowball.spawnAt(origin)
            snowball.world.playSound(snowball.location, Sound.BLOCK_BUBBLE_COLUMN_BUBBLE_POP, 1.0f, 1.0f)
            snowballs.add(snowball)
        }
        snowballRepeatExecutor(snowballs)
    }

    private fun cloneSnowball(snowball: Snowball, newVelocity: Vector): Snowball {
        val newSnowball = snowball.world.createEntity(snowball.location, Snowball::class.java)
        newSnowball.velocity = newVelocity
        newSnowball.shooter = snowball.shooter
        newSnowball.item = snowball.item
        newSnowball.isPersistent = false
        newSnowball.ticksLived = snowball.ticksLived
        Util.setPersistentKey(newSnowball, nameSpace, PersistentDataType.SHORT, 1)
        newSnowball.spawnAt(snowball.location)
        snowballRepeatExecutor(newSnowball)
        return newSnowball
    }

    private fun repeatExecutorLogic(snowball: Snowball): Boolean {
        if (snowball.isDead || snowball.location.block.isLiquid) {
            if (!snowball.isDead) {
                Executors.sync { snowball.remove() }
            }
            return true
        }
        val particleDisplay = COLORS.values.find { it.first == snowball.item }?.second ?: return false
        particleDisplay.spawn(snowball.location)
        return false
    }

    private fun snowballRepeatExecutor(snowballs: Collection<Snowball>) {
        Executors.asyncTimer(0, 2) { task ->
            for (snowball in snowballs) {
                if (repeatExecutorLogic(snowball)) {
                    task.cancel()
                    return@asyncTimer
                }
            }
        }
    }

    private fun snowballRepeatExecutor(snowball: Snowball) {
        Executors.asyncTimer(0, 2) { task ->
            if (repeatExecutorLogic(snowball)) {
                task.cancel()
                return@asyncTimer
            }
        }
    }
}