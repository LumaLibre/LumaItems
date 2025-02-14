package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.NeedsEdits
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

//@Disable(value = [
//    WorldName.MAIN,
//    WorldName.MAIN_NETHER,
//    WorldName.MAIN_THE_END,
//    WorldName.MAIN_SEASONS,
//    WorldName.RESOURCE,
//    WorldName.RESOURCE_NETHER,
//    WorldName.SPAWN,
//    WorldName.EVENT,
//    WorldName.EVENT_NEW,
//    WorldName.EVENT_THE_END
//])
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
            .quotes("<#8A4E4E>\"<#8A5254>C<#8A575A>h<#8A5B60>o<#8A6066>o<#8A646C>s<#8A6973>e <#8A727F>i<#8A7685>t<#8A7B8B>, <#8B8397>c<#8B889D>h<#8B8CA3>i<#8B91A9>s<#8B95AF>e<#8B9AB6>l <#8BA3C2>i<#8BA7C8>t<#8BACCE>.<#8BB0D4>\"")
            .tier(Tier.WINTER_2024)
            .stringPersistentDatas(mutableMapOf(key to Mode.LOGS.name))
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
            LumaItems.log("Frostbark Chisel: Unsupported drop amount.")
            LumaItems.log("Drops: ${drops.joinToString { "${it.type.name}x${it.amount}" }}")
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

    private fun setWoodTypeFromMode(mode: Mode, block: ItemStack): ItemStack {
        var materialName = block.type.name
        if (!materialName.contains("_LOG") && !materialName.contains("_STEM")) return block
        else if (mode == Mode.LOGS) return block

        materialName = materialName.replace("_LOG", "").replace("_STEM", "")
        val simpleWoodType = Util.enumValueOfOrNull(SimpleWoodType::class.java, materialName) ?: return block
        block.type = simpleWoodType.asMaterial(mode)
        return block
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
        player.sendActionBar(MiniMessageUtil.mm("<#f498f6>${Util.formatMaterialName(newMode.name)}"))
    }

    enum class Mode {
        LOGS,
        STRIPPED_LOGS,
        WOOD,
        STRIPPED_WOOD
    }

    enum class SimpleWoodType(val logName: String, val woodName: String) {
        OAK("LOG", "WOOD"),
        SPRUCE("LOG", "WOOD"),
        BIRCH("LOG", "WOOD"),
        JUNGLE("LOG", "WOOD"),
        ACACIA("LOG", "WOOD"),
        DARK_OAK("LOG", "WOOD"),
        MANGROVE("LOG", "WOOD"),
        CHERRY("LOG", "WOOD"),
        WARPED("STEM", "HYPHAE"),
        CRIMSON("STEM", "HYPHAE");

        fun asMaterial(mode: Mode): Material {
            return when (mode) {
                Mode.LOGS -> Material.valueOf("${this.name}_$logName")
                Mode.STRIPPED_LOGS -> Material.valueOf("STRIPPED_${this.name}_$logName")
                Mode.WOOD -> Material.valueOf("${this.name}_$woodName")
                Mode.STRIPPED_WOOD -> Material.valueOf("STRIPPED_${this.name}_$woodName")
            }
        }
    }
}