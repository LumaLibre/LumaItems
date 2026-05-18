package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.PersistentDataRecord
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.actionBar
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

class FrostbarkChiselItem : CustomItemFunctions() {


    companion object {
        private val key = Util.namespacedKey("mode")
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#8A4E4E>F<#8A5A5F>r<#8A6770>o<#8A7380>s<#8B7F91>t<#8B8BA2>b<#8B98B3>a<#8BA4C3>r<#8BB0D4>k <#8BB0D4>C<#8BB0D4>h<#8BB0D4>i<#8BB0D4>s<#8BB0D4>e<#8BB0D4>l</b>")
            .material(Material.NETHERITE_AXE)
            .lore(
                "Broken logs may drop as",
                "any type of tree wood.",
                "",
                "<gold>Sneak & right-click <white>to set",
                "the type of wood to drop."
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 7,
                Enchantment.UNBREAKING to 4,
                Enchantment.FORTUNE to 5,
                Enchantment.SMITE to 5,
                Enchantment.MENDING to 1
            )
            .tagline("<#8A4E4E>\"<#8A5254>C<#8A575A>h<#8A5B60>o<#8A6066>o<#8A646C>s<#8A6973>e <#8A727F>i<#8A7685>t<#8A7B8B>, <#8B8397>c<#8B889D>h<#8B8CA3>i<#8B91A9>s<#8B95AF>e<#8B9AB6>l <#8BA3C2>i<#8BA7C8>t<#8BACCE>.<#8BB0D4>\"")
            .tier(Tier.WINTER_2024)
            .persistentDataRecords(PersistentDataRecord.create(key, PersistentDataType.STRING, Mode.LOGS.name))
            .persistentData("frostbark-chisel")
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (player.isSneaking) {
            swapMode(event.item ?: return, player)
        }
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val axe = player.inventory.itemInMainHand
        val strName = event.block.type.name
        if (!strName.endsWith("_LOG") && !strName.endsWith("_STEM")) {
            return
        }
        val drops = event.block.getDrops(axe).ifEmpty { return }

        if (drops.size > 1) {
            return
        }

        event.isDropItems = false
        player.world.dropItemNaturally(event.block.location, setWoodTypeFromMode(getMode(axe), drops.first()))
    }


    private fun getMode(axe: ItemStack): Mode {
        return axe.itemMeta?.persistentDataContainer?.get(key, PersistentDataType.STRING)
            ?.let { Util.enumValueOfOrNull(Mode::class.java, it) }
            ?: Mode.LOGS
    }

    @Suppress("DEPRECATION")
    private fun setWoodTypeFromMode(mode: Mode, item: ItemStack): ItemStack {
        var materialName = item.type.name
        if (!materialName.contains("_LOG") && !materialName.contains("_STEM")) return item
        else if (mode == Mode.LOGS) return item

        materialName = materialName.replace("_LOG", "").replace("_STEM", "")
        val simpleWoodType = Util.enumValueOfOrNull(SimpleWoodType::class.java, materialName) ?: return item
        item.type = simpleWoodType.asMaterial(mode) ?: return item
        return item
    }

    private fun swapMode(axe: ItemStack, player: Player) {
        val mode = axe.itemMeta?.persistentDataContainer?.get(key, PersistentDataType.STRING)
            ?.let { Util.enumValueOfOrNull(Mode::class.java, it) }
            ?: Mode.LOGS

        val newMode = Mode.entries.toTypedArray().let { modes ->
            val index = (modes.indexOf(mode) + 1) % modes.size
            modes[index]
        }

        val newMeta = axe.itemMeta?.apply { persistentDataContainer.set(key, PersistentDataType.STRING, newMode.name) }
            ?: return
        axe.itemMeta = newMeta
        player.actionBar("<#f498f6>${Util.formatEnumerator(newMode.name)}")
    }

    enum class Mode {
        LOGS,
        STRIPPED_LOGS,
        WOOD,
        STRIPPED_WOOD
    }

    enum class SimpleWoodType(val log: Material, val wood: Material) {
        OAK(Material.OAK_LOG, Material.OAK_WOOD),
        SPRUCE(Material.SPRUCE_LOG, Material.SPRUCE_WOOD),
        BIRCH(Material.BIRCH_LOG, Material.BIRCH_WOOD),
        JUNGLE(Material.JUNGLE_LOG, Material.JUNGLE_WOOD),
        ACACIA(Material.ACACIA_LOG, Material.ACACIA_WOOD),
        DARK_OAK(Material.DARK_OAK_LOG, Material.DARK_OAK_WOOD),
        MANGROVE(Material.MANGROVE_LOG, Material.MANGROVE_WOOD),
        CHERRY(Material.CHERRY_LOG, Material.CHERRY_WOOD),
        WARPED(Material.WARPED_STEM, Material.WARPED_HYPHAE),
        CRIMSON(Material.CRIMSON_STEM, Material.CRIMSON_HYPHAE),;

        fun asMaterial(mode: Mode): Material? {
            return when (mode) {
                Mode.LOGS -> this.log
                Mode.WOOD -> this.wood
                Mode.STRIPPED_LOGS -> Material.matchMaterial("STRIPPED_${this.log.name}")
                Mode.STRIPPED_WOOD -> Material.matchMaterial("STRIPPED_${this.wood.name}")
            }
        }
    }
}