package dev.lumas.lumaitems.items.tools.harrow

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.MiniMessageUtil
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
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

        private val CROP_LIST: List<CropOfTheDay> = listOf(
            CropOfTheDay.of(Material.POTATOES, "#c8963a"),
            CropOfTheDay.of(Material.CARROTS, "#ff8c09"),
            CropOfTheDay.of(Material.WHEAT, "#dcba65"),
            CropOfTheDay.of(Material.BEETROOTS, "#a4272b"),
            CropOfTheDay.of(Material.NETHER_WART, "#a5242f"),
            CropOfTheDay.of(Material.SUGAR_CANE, "#4b8c2a"),
            CropOfTheDay.of(listOf(Material.RED_MUSHROOM, Material.BROWN_MUSHROOM), listOf("#FF5858", "#d09c7c"), "Mushrooms"),
        )
    }


    private val cropOfTheDay: CropOfTheDay = fun (): CropOfTheDay {
        val today = LocalDate.now()
        val dayOfMonth = today.dayOfMonth // 1 to 31

        val cropIndex = (dayOfMonth - 1) % CROP_LIST.size
        return CROP_LIST[cropIndex]
    }();

    private val dustOptions = Particle.DustOptions(BukkitColor.WHITE, 1f)

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ABF2F9:#BEB4E6:#FFBAC3:#FFE8BA:#A3EABD>Blooming Bunny Rake</gradient></b>")
            .customEnchants("<#ABF2F9>Stalk Days")
            .tagline("<gradient:#FFBAC3:#FFE8BA:#A3EABD>\"Get yer favorite crop!\"</gradient>")
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
        val msg = "<${cropOfTheDay.color()}>${MESSAGES.random().format(cropOfTheDay.name)}"
        player.sendActionBar(MiniMessageUtil.mm(msg))
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (!cropOfTheDay.materials.contains(block.type) || random().nextInt(100) < 85) { // TODO: Come back to this
            return
        }

        event.isDropItems = false
        val drops = block.getDrops(player.inventory.itemInMainHand).toList()
        block.world.spawnParticle(Particle.DUST, block.location, 5, 0.5, 0.5, 0.5, dustOptions)

        // 2x half our drops
        for (i in 0..drops.size / 2) {
            val drop = drops[i]
            drop.amount *= 2
        }

        drops.forEach { drop ->
            block.world.dropItemNaturally(block.location, drop)
        }
    }

    private data class CropOfTheDay(
        val materials: List<Material>,
        val hexColors: List<String>,
        val name: String = run {
            if (materials.isEmpty()) {
                return@run "Unknown"
            }
            Util.formatEnumerator(materials[0].name)
        },
    ) {
        fun color(): String {
            if (hexColors.isEmpty()) {
                return "#FFFFFF"
            }
            return hexColors.random()
        }

        companion object {
            fun of(material: Material, hexColor: String): CropOfTheDay {
                return CropOfTheDay(
                    materials = listOf(material),
                    hexColors = listOf(hexColor)
                )
            }
            fun of(materials: List<Material>, hexColors: List<String>, name: String): CropOfTheDay {
                return CropOfTheDay(
                    materials = materials,
                    hexColors = hexColors,
                    name = name
                )
            }
        }
    }
}