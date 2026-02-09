package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.extensions.BlockUtil.breakNaturallyWithLog
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class IllumeShearsItem : CustomItemFunctions() {

    private companion object {
        private val lightBlocks by lazy {
            Material.entries
                .filter { it.isBlock && it.createBlockData().lightEmission > 0 }
                .toList()
        }

        init {
            Executors.async {
                // Preload light blocks
                lightBlocks
            }
        }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#C5ADFF:#8F79F8:#B36EAF:#DB6B90:#77C679:#CBF6B7>Illume Shears</gradient></b>")
            .customEnchants("<#c5adff>Quick-break")
            .persistentData("illume-shears")
            .material(Material.SHEARS)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 5,
                Enchantment.SILK_TOUCH to 5,
            )
            .lore(
                "<#c5adff>Left-click</#c5adff> to instantly",
                "break light-emitting",
                "blocks."
            )
            .tier(Tier.DEBUG)
            .buildPair()
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return

        if (block.getBreakSpeed(player) == Float.POSITIVE_INFINITY) return

        if (block.type in lightBlocks) {
            block.breakNaturallyWithLog(player, player.inventory.itemInMainHand)
        }
    }
}