package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.disabling.Disable
import dev.lumas.lumaitems.util.disabling.WorldName
import kotlin.random.Random
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW)
class DarkRabbitHatchetItem : CustomItem {

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#1B1024&lD&#33243F&la&#4B375A&lr&#634B75&lk &#7B5F91&lR&#9372AC&la&#AB86C7&lb&#C399E2&lb&#DBADFD&li&#DAA7FA&lt &#D9A2F7&lH&#D89CF3&la&#D796F0&lt&#D590ED&lc&#D48BEA&lh&#D385E6&le&#D27FE3&lt",
            mutableListOf("&#C399E2Chancity"),
            mutableListOf(
                "Upon breaking blocks, drops will",
                "randomly be converted to charcoal",
                "or they will be multiplied."
            ),
            Material.NETHERITE_AXE,
            mutableListOf("darkrabbithatchet"),
            mutableMapOf(
                Enchantment.EFFICIENCY to 8,
                Enchantment.UNBREAKING to 9,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1,
                Enchantment.SMITE to 5
            )
        )
        item.addQuote("&#C399E2\"&#BF96DEE&#BB93D9u&#B790D5g&#B48CD0h&#B089CC, &#AC86C8i&#A883C3t&#A480BF'&#A07DBBs &#9C79B6g&#9876B2o&#9573ADt &#9170A9c&#8D6DA5h&#896AA0a&#85669Cr&#816397c&#7D6093o&#795D8Fa&#765A8Al &#725786a&#6E5381l&#6A507Dl &#664D79o&#624A74v&#5E4770e&#5A446Cr &#574067i&#533D63t&#4F3A5E!&#4B375A\"")
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"
        return Pair("darkrabbithatchet", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.BREAK_BLOCK -> {
                if (Random.Default.nextInt(245) > 3) {
                    return false
                }
                event as BlockBreakEvent
                event.isDropItems = false
                chancityAbility(event.block.getDrops(player.inventory.itemInMainHand), event.block)
            }

            else -> return false
        }
        return true
    }

    private fun chancityAbility(drops: Collection<ItemStack>, block: Block) {
        var doDropCharCoal = Random.Default.nextBoolean()
        val doMultiplyDrops = Random.Default.nextBoolean()

        if (!doMultiplyDrops && !doDropCharCoal) {
            doDropCharCoal = true
        }

        if (doMultiplyDrops) {
            val itemStack = drops.iterator().next()
            itemStack.amount *= Random.Default.nextInt(2, 6)
        }

        if (doDropCharCoal) {
            for (drop in drops) {
                drop.setType(Material.CHARCOAL)
            }
        }

        for (drop in drops) {
            block.world.dropItemNaturally(block.location, drop)
        }


        val color = if (doDropCharCoal) Color.BLACK else block.blockData.mapColor
        block.world.spawnParticle(Particle.DUST, block.location.add(0.5, 0.5, 0.5), 20, 0.5, 0.5, 0.5, 0.1, Particle.DustOptions(color, 1f))
        block.world.playSound(block.location, Sound.ENTITY_GLOW_SQUID_AMBIENT,0.6f, 1f)
    }


}