package dev.lumas.lumaitems.items.tools.nests

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.obj.PersistentDataRecord
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class MontanaireSpadeBundleItem : CustomItemFunctions() {

    private val parent = MontanaireSpadeItem()

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#EC4D4D:#F69B27:#FAF252:#70F36B:#5780F1>Montanaire Spade</gradient></b> <!b><#F7FFC9>Bundle</#F7FFC9></!b>")
            .material(Material.BLACK_BUNDLE)
            .tier(Tier.CHRISTMAS_2025)
            .persistentData("montanaire-spade-bundle")
            .vanillaEnchants(
                Enchantment.SILK_TOUCH to 1,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1
            )
            .lore(
                "<dark_gray>Right-click to open.",
                "",
                "A spade great for",
                "gathering colorful",
                "types of concrete",
                "powder.",
                "",
                "Breaking sand with",
                "this spade will yield",
                "concrete powder of",
                "the spade's color."
            )
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val item = event.item ?: return
        item.amount -= 1

        player.playSound(player.location, Sound.ITEM_ARMOR_EQUIP_NETHERITE, 1f, 1f)
        Util.giveItem(player, parent.createItem().second)
    }
}

class MontanaireSpadeItem : CustomItemFunctions() {

    companion object {
        private val COLOR_KEY = Util.namespacedKey("montanaire-spade-color")
    }

    override fun createItem(): Pair<String, ItemStack> {
        val colorBatch = ColorBatch.entries.random()

        return ItemFactory.builder()
            .name("<b><gradient:#EC4D4D:#F69B27:#FAF252:#70F36B:#5780F1>Montanaire Spade</gradient></b>")
            .customEnchants("<${colorBatch.color}>Binder")
            .material(Material.NETHERITE_SHOVEL)
            .tier(Tier.CHRISTMAS_2025)
            .persistentData("montanaire-spade")
            .persistentDataRecords(
                PersistentDataRecord.create(COLOR_KEY, PersistentDataType.STRING, colorBatch.name)
            )
            .vanillaEnchants(
                Enchantment.SILK_TOUCH to 1,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1
            )
            .lore(
                "A spade great for",
                "gathering colorful",
                "types of concrete",
                "powder.",
                "",
                "Breaking sand with",
                "this spade will yield",
                "concrete powder of",
                "the spade's <${colorBatch.color}>color</${colorBatch.color}>."
            )
            .buildPair()
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (block.type != Material.SAND && block.type != Material.RED_SAND) return

        val colorBatch = player.inventory.itemInMainHand
            .persistentDataContainer
            .get(COLOR_KEY, PersistentDataType.STRING)
            ?.let { ColorBatch.fromKey(it) }
            ?: return

        event.isDropItems = false
        block.world.dropItemNaturally(block.location.toCenterLocation(), colorBatch.itemStack)
    }

    enum class ColorBatch(
        val itemStack: ItemStack,
        val color: String
    ) {
        WHITE(ItemStack.of(Material.WHITE_CONCRETE_POWDER), "#dcdede"),
        LIGHT_GRAY(ItemStack.of(Material.LIGHT_GRAY_CONCRETE_POWDER), "#9c9c96"),
        GRAY(ItemStack.of(Material.GRAY_CONCRETE_POWDER), "#4d5558"),
        BLACK(ItemStack.of(Material.BLACK_CONCRETE_POWDER), "#252529"),
        BROWN(ItemStack.of(Material.BROWN_CONCRETE_POWDER),  "#85532e"),
        RED(ItemStack.of(Material.RED_CONCRETE_POWDER), "#c42c2f"),
        ORANGE(ItemStack.of(Material.ORANGE_CONCRETE_POWDER), "#f27700"),
        YELLOW(ItemStack.of(Material.YELLOW_CONCRETE_POWDER), "#f0c200"),
        LIME(ItemStack.of(Material.LIME_CONCRETE_POWDER), "#67ba00"),
        GREEN(ItemStack.of(Material.GREEN_CONCRETE_POWDER), "#5c7627"),
        CYAN(ItemStack.of(Material.CYAN_CONCRETE_POWDER), "#00a6a9"),
        LIGHT_BLUE(ItemStack.of(Material.LIGHT_BLUE_CONCRETE_POWDER), "#00b5d9"),
        BLUE(ItemStack.of(Material.BLUE_CONCRETE_POWDER), "#484cb2"),
        PURPLE(ItemStack.of(Material.PURPLE_CONCRETE_POWDER), "#882fb3"),
        MAGENTA(ItemStack.of(Material.MAGENTA_CONCRETE_POWDER), "#d457c4"),
        PINK(ItemStack.of(Material.PINK_CONCRETE_POWDER), "#e78caa");

        fun formattedName() = Util.formatEnumerator(this.name)

        companion object {
            fun fromKey(key: String): ColorBatch? = ColorBatch.entries.firstOrNull { it.name == key.uppercase() }
        }
    }

}