package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

@Disable(WorldName.EVENT_NEW)
class AutumnsHarrowerItem : CustomItem {

    companion object {
        private val crops: Map<Material, Color> = mapOf(
            Material.WHEAT to Color.fromRGB(220, 187, 101),
            Material.BEETROOT to Color.fromRGB(164, 39, 44),
            Material.CARROT to Color.fromRGB(255, 142, 9),
            Material.POTATO to Color.fromRGB(200, 151, 58),
            Material.NETHER_WART to Color.fromRGB(165, 36, 47)
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#cd3c33&lA&#d2452e&lu&#d64d2a&lt&#db5625&lu&#e05e21&lm&#e4671c&ln&#e96f18&l'&#ee7813&ls &#f18010&lH&#f2880f&la&#f4900e&lr&#f5980d&lr&#f79f0c&lo&#f8a70a&lw&#faaf09&le&#fbb708&lr",
            mutableListOf("&#e95e10F&#eb670fu&#ed700el&#ee790el &#f0820dH&#f28b0ca&#f4930br&#f69c0av&#f7a50ae&#f9ae09s&#fbb708t"),
            mutableListOf("&#cd3c33\"&#cf4032L&#d04430e&#d2482ft &#d34c2dr&#d5512ci&#d6552ac&#d85929h&#d95d28e&#db6126s &#dc6525f&#de6923a&#df6d22l&#e17120l &#e2751fl&#e47a1ei&#e67e1ck&#e7821be &#e98619a&#ea8a18u&#ec8e16t&#ed9215u&#ef9613m&#f09a12n &#f29e11l&#f3a30fe&#f5a70ea&#f6ab0cv&#f8af0be&#f9b309s&#fbb708\"","","Breaking crops with this hoe", "will occasionally yield a", "surplus amount of drops"),
            Material.NETHERITE_HOE,
            mutableListOf("autumnharrower"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 10, Enchantment.FORTUNE to 5, Enchantment.EFFICIENCY to 7)
        )
        item.tier = "&#c46bfb&lH&#c86eee&la&#cd71e2&ll&#d174d5&ll&#d677c8&lo&#da7abc&lm&#de7daf&la&#e380a2&lr&#e78395&le&#eb8689&ls &#f0897c&l2&#f48c6f&l0&#f98f63&l2&#fd9256&l3"
        return Pair("autumnharrower", item.createItem())
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

        if (crops.containsKey(item.type)) {
            block.world.spawnParticle(
                Particle.DUST, block.location, 100, 0.5, 0.5, 0.5, DustOptions(crops[item.type]!!, 1f)
            )
            block.world.playSound(block.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 1f, 1f)
            if (Random.nextBoolean()) {
                topHarvestAnimation(block.location, item.type, DustOptions(crops[item.type]!!, 1f))
            }
            block.world.dropItem(block.location.add(Random.nextDouble(0.1),Random.nextDouble(0.1),Random.nextDouble(0.1)),
                ItemStack(item.type, 300))
        }
    }

    private fun topHarvestAnimation(location: Location, material: Material, dustOptions: DustOptions) {
        val loc = location.add(0.0,0.2,0.0).toCenterLocation()
        val task = Bukkit.getScheduler().scheduleSyncRepeatingTask(instance(), {
            loc.world.dropItem(loc, ItemStack(material))
            loc.world.spawnParticle(Particle.DUST, loc, 30, 0.2, 0.2, 0.2, dustOptions)
        }, 0, 5)

        Bukkit.getScheduler().scheduleSyncDelayedTask(instance(), {
            Bukkit.getScheduler().cancelTask(task)
        }, 150)
    }

}