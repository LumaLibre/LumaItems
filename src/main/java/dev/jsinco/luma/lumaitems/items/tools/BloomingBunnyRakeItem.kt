package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.inventory.ItemStack
import java.time.LocalDate
import org.bukkit.Color as BukkitColor

class BloomingBunnyRakeItem : CustomItemFunctions() {

    companion object {
        private val MESSAGES = listOf(
            "Let's farm %s!",
            "Today's crop is %s!",
            "Get your %s!",
            "It's time to farm %s!",
            "Harvest some %s!",
        )

        private val CROP_MAP: Map<Material, String> = mapOf(
            Material.POTATOES to "#c8963a",
            Material.CARROTS to "#ff8c09",
            Material.WHEAT to "#dcba65",
            Material.BEETROOTS to "#a4272b",
            Material.NETHER_WART to "#a5242f",
            //Material.COCOA to "#57331a",
            Material.SUGAR_CANE to "#4b8c2a"
        )
    }


    private val cropOfTheDay: Material = fun (): Material {
        val today = LocalDate.now()
        val dayOfMonth = today.dayOfMonth // 1 to 31

        val list = CROP_MAP.keys.toList()

        val cropIndex = (dayOfMonth - 1) % list.size
        return list[cropIndex]
    }();

    private val dustOptions = Particle.DustOptions(BukkitColor.WHITE, 1f)

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ABF2F9:#BEB4E6:#FFBAC3:#FFE8BA:#A3EABD>Blooming Bunny Rake</gradient></b>")
            .customEnchants("<#ABF2F9>Stalk Days")
            .quotes("<gradient:#FFBAC3:#FFE8BA:#A3EABD>\"Get yer favorite crop!\"</gradient>")
            .material(Material.NETHERITE_HOE)
            .persistentData("blooming-bunny-rake")
            .tier(Tier.EASTER_2025)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 6,
                Enchantment.MENDING to 1,
                Enchantment.EFFICIENCY to 7,
                Enchantment.FORTUNE to 5
            )
            .lore( // </#ABF2F9>
                "This rake boosts material",
                "drops for various crops",
                "depending on the day.",
            )
            .buildPair()
    }

    override fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {
        val cropName = Util.formatMaterialName(cropOfTheDay.toString())
        val msg = "<${CROP_MAP[cropOfTheDay]}>${MESSAGES.random().format(cropName)}"
        player.sendActionBar(MiniMessageUtil.mm(msg))
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (block.type != cropOfTheDay || random().nextInt(100) < 75) {
            return
        }

        event.isDropItems = false
        val drops = block.getDrops(player.inventory.itemInMainHand)
        block.world.spawnParticle(Particle.DUST, block.location, 5, 0.5, 0.5, 0.5, dustOptions)
        val multiplier = random().nextInt(2, 4)
        drops.forEach { drop ->
            block.world.dropItemNaturally(block.location, drop.asQuantity(drop.amount * multiplier))
        }
    }
}