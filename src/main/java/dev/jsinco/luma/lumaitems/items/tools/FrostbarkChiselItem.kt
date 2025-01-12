package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.NeedsEdits
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType

@NeedsEdits
class FrostbarkChiselItem : CustomItemFunctions() {


    companion object {
        private val key = Util.namespacedKey("mode")
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#8A4E4E>F<#8A5A5F>r<#8A6770>o<#8A7380>s<#8B7F91>t<#8B8BA2>b<#8B98B3>a<#8BA4C3>r<#8BB0D4>k <#8BB0D4>C<#8BB0D4>h<#8BB0D4>i<#8BB0D4>s<#8BB0D4>e<#8BB0D4>l</b>")
            .material(Material.NETHERITE_AXE)
            .lore(
                "Broken logs may drop as any",
                "type of tree wood.",
                "",
                "<gold>Sneak & right-click <white>to",
                "set what type of wood should drop."
            )
            .vanillaEnchants()
            .quotes("<#8A4E4E>\"<#8A5354>C<#8A575B>h<#8A5C61>o<#8A6168>o<#8A656E>s<#8A6A74>e <#8A7381>i<#8A7887>t<#8A7D8E>, <#8B869B>C<#8B8BA1>h<#8B8FA7>i<#8B94AE>s<#8B99B4>e<#8B9DBA>l <#8BA7C7>i<#8BABCE>t<#8BB0D4>\"\n")
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
        val drops = event.block.getDrops(axe)

        event.isDropItems = false
        drops.forEach { drop ->
            setWoodTypeFromMode(getMode(axe), drop)
            player.world.dropItemNaturally(event.block.location, drop)
        }

    }


    private fun getMode(axe: ItemStack): Mode {
        return axe.itemMeta?.persistentDataContainer?.get(key, PersistentDataType.STRING)
            ?.let { Util.enumValueOfOrNull(Mode::class.java, it) }
            ?: Mode.LOGS
    }

    private fun setWoodTypeFromMode(mode: Mode, block: ItemStack) {
        var materialName = block.type.name
        println(materialName)
        if (!materialName.contains("_LOG") && !materialName.contains("_STEM")) return

        materialName = materialName.replace("_LOG", "").replace("_STEM", "")
        println(materialName)
        val simpleWoodType = Util.enumValueOfOrNull(SimpleWoodType::class.java, materialName) ?: return

        block.type = simpleWoodType.asMaterial(mode)
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
        MiniMessageUtil.msg(player, "<green>Mode set to <white>${newMode.name.lowercase()}")
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