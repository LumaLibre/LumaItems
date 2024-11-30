package dev.jsinco.lumaitems.items.tools

import dev.jsinco.lumaitems.LumaItems
import dev.jsinco.lumaitems.items.ItemFactory
import dev.jsinco.lumaitems.enums.Action
import dev.jsinco.lumaitems.manager.CustomItem
import dev.jsinco.lumaitems.util.AbilityUtil
import dev.jsinco.lumaitems.util.disabling.Disable
import dev.jsinco.lumaitems.util.disabling.WorldName
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

@Disable(WorldName.EVENT_NEW)
class AutumnsMattockItem : CustomItem {

    companion object {
        private val plugin: LumaItems = LumaItems.getInstance()

        private val oreColors: Map<Material, Color> = mapOf(
            Material.COAL to Color.fromRGB(33, 34, 31),
            Material.RAW_COPPER to Color.fromRGB(179, 92, 62),
            Material.LAPIS_LAZULI to Color.fromRGB(16, 67, 169),
            Material.RAW_IRON to Color.fromRGB(216, 175, 147),
            Material.RAW_GOLD to Color.fromRGB(191, 154, 31),
            Material.REDSTONE to Color.fromRGB(171, 1, 3),
            Material.DIAMOND to Color.fromRGB(124, 208, 200),
            Material.EMERALD to Color.fromRGB(43, 156, 82),
            // Nether ores
            Material.GOLD_NUGGET to Color.fromRGB(191, 154, 31),
            Material.QUARTZ to Color.fromRGB(185, 160, 154),
            //Material.ANCIENT_DEBRIS to Color.fromRGB(77, 56, 50)
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#cd3c33&lA&#d2452e&lu&#d74e29&lt&#dc5724&lu&#e16120&lm&#e66a1b&ln&#eb7316&l'&#f07c11&ls &#f28410&lM&#f38d0e&la&#f5950d&lt&#f69e0c&lt&#f8a60b&lo&#f9af09&lc&#fbb708&lk",
            mutableListOf("&#e95e10F&#eb670fu&#ed700el&#ee790el &#f0820dH&#f28b0ca&#f4930br&#f69c0av&#f7a50ae&#f9ae09s&#fbb708t"),
            mutableListOf("&#cd3c33\"&#cf4032L&#d04430e&#d2482ft &#d34c2dr&#d5512ci&#d6552ac&#d85929h&#d95d28e&#db6126s &#dc6525f&#de6923a&#df6d22l&#e17120l &#e2751fl&#e47a1ei&#e67e1ck&#e7821be &#e98619a&#ea8a18u&#ec8e16t&#ed9215u&#ef9613m&#f09a12n &#f29e11l&#f3a30fe&#f5a70ea&#f6ab0cv&#f8af0be&#f9b309s&#fbb708\"","","Breaking ores with this pickaxe", "will occasionally yield a", "surplus amount of drops"),
            Material.NETHERITE_PICKAXE,
            mutableListOf("autumnsmattock"),
            mutableMapOf(Enchantment.UNBREAKING to 10, Enchantment.FORTUNE to 6, Enchantment.MENDING to 1, Enchantment.EFFICIENCY to 7)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("autumnsmattock", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val random = if (player.scoreboardTags.contains("lumaitems.debug")) 0 else Random.nextInt(500)
        if (random > 5) return false
        when (type) {
            Action.BREAK_BLOCK -> {
                val blockBreakEvent = event as BlockBreakEvent
                fullHarvest(blockBreakEvent.block, blockBreakEvent.block.drops)
            }

            else -> return false
        }
        return true
    }

    private fun fullHarvest(block: Block, drops: Collection<ItemStack>) {
        if (drops.isEmpty()) return
        val item = AbilityUtil.findMostCommonItem(drops)

        if (oreColors.containsKey(item.type)) {
            block.world.spawnParticle(
                Particle.DUST, block.location, 100, 0.5, 0.5, 0.5,
                Particle.DustOptions(oreColors[item.type]!!, 1f)
            )
            block.world.playSound(block.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 1f, 1f)
            if (Random.nextBoolean()) {
                topHarvestAnimation(
                    block.location, item.type,
                    Particle.DustOptions(oreColors[item.type]!!, 1f)
                )
            }
            val amt = if (item.type == Material.ANCIENT_DEBRIS) 3 else 90
            block.world.dropItem(block.location.add(Random.nextDouble(0.1), Random.nextDouble(0.1), Random.nextDouble(0.1)), ItemStack(item.type, amt))
        }
    }

    private fun topHarvestAnimation(location: Location, material: Material, dustOptions: Particle.DustOptions) {
        val loc = location.add(0.0, 0.2, 0.0).toCenterLocation()
        val period: Long = if (material == Material.ANCIENT_DEBRIS) 20 else 8
        val task = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, {
            loc.world.dropItem(loc, ItemStack(material))
            loc.world.spawnParticle(Particle.DUST, loc, 30, 0.2, 0.2, 0.2, dustOptions)
        }, 0, period)

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, {
            Bukkit.getScheduler().cancelTask(task)
        }, 150)
    }

}