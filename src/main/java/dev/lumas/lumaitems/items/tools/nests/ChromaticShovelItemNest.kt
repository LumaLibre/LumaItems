package dev.lumas.lumaitems.items.tools.nests

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.model.PersistentDataRecord
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.annotations.Ignore
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@Ignore
class ChromaticShovelItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("chromatic-shovel-colors")
    }

    override fun createItem(): Pair<String, ItemStack> {
        // pick 3 random colors
        val colors = ColorBatch.entries.toTypedArray()
            .sortedBy { Math.random() }
            .take(3)

        return ItemFactory.builder()
            .name("<b>${ColorBatch.colorsToGradient(colors)}Chromatic Shovel</gradient></b>")
            .material(Material.NETHERITE_SHOVEL)
            .persistentData("chromatic-shovel")
            .persistentDataRecords(
                PersistentDataRecord.create(KEY, PersistentDataType.INTEGER_ARRAY, colors.map { ColorBatch.entries.indexOf(it) }.toIntArray())
            )
            .buildPair()
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (block.type != Material.SAND && block.type != Material.RED_SAND) return

        val item = player.inventory.itemInMainHand
        val colorIndexes = Util.getPersistentKey(item, KEY, PersistentDataType.INTEGER_ARRAY) ?: return
        val colors = colorIndexes.map { index ->
            ColorBatch.entries.getOrNull(index)
        }.filterNotNull()

        if (colors.isEmpty()) return

        event.isDropItems = false
        block.world.dropItemNaturally(block.location.toCenterLocation(), colors.random().itemStack)
    }
    
    
    enum class ColorBatch(
        val itemStack: ItemStack,
        val color: String
    ) {
        WHITE(ItemStack.of(Material.WHITE_STAINED_GLASS), "#dcdede"),
        LIGHT_GRAY(ItemStack.of(Material.LIGHT_GRAY_STAINED_GLASS), "#9c9c96"),
        GRAY(ItemStack.of(Material.GRAY_STAINED_GLASS), "#4d5558"),
        BLACK(ItemStack.of(Material.BLACK_STAINED_GLASS), "#252529"),
        BROWN(ItemStack.of(Material.BROWN_STAINED_GLASS),  "#85532e"),
        RED(ItemStack.of(Material.RED_STAINED_GLASS), "#c42c2f"),
        ORANGE(ItemStack.of(Material.ORANGE_STAINED_GLASS), "#f27700"),
        YELLOW(ItemStack.of(Material.YELLOW_STAINED_GLASS), "#f0c200"),
        LIME(ItemStack.of(Material.LIME_STAINED_GLASS), "#67ba00"),
        GREEN(ItemStack.of(Material.GREEN_STAINED_GLASS), "#5c7627"),
        CYAN(ItemStack.of(Material.CYAN_STAINED_GLASS), "#00a6a9"),
        LIGHT_BLUE(ItemStack.of(Material.LIGHT_BLUE_STAINED_GLASS), "#00b5d9"),
        BLUE(ItemStack.of(Material.BLUE_STAINED_GLASS), "#484cb2"),
        PURPLE(ItemStack.of(Material.PURPLE_STAINED_GLASS), "#882fb3"),
        MAGENTA(ItemStack.of(Material.MAGENTA_STAINED_GLASS), "#d457c4"),
        PINK(ItemStack.of(Material.PINK_STAINED_GLASS), "#e78caa");


        companion object {
            fun colorsToGradient(colors: List<ColorBatch>): String {
                return "<gradient:${colors.joinToString(":") { it.color }}>"
            }
        }
    }
}