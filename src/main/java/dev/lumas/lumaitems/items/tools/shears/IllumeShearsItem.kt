package dev.lumas.lumaitems.items.tools.shears

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.tags.Kind
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class IllumeShearsItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#C5ADFF:#8F79F8:#B36EAF:#DB6B90:#77C679:#CBF6B7>Illume Shears</gradient></b>")
            .customEnchants("<#c5adff>Quick-break")
            .persistentData("illume-shears")
            .material(Material.SHEARS)
            .tier(Tier.VALENTIDE_2026)
            .vanillaEnchants(
                Enchantment.UNBREAKING to 5,
                Enchantment.SILK_TOUCH to 5,
                Enchantment.MENDING to 1
            )
            .lore(
                "<#c5adff>Left-click</#c5adff> any light",
                "emitting block to",
                "instantly destroy it."
            )
            .buildPair()
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return

        if (block.canBreak(player.inventory.itemInMainHand) && block.getBreakSpeed(player) != Float.POSITIVE_INFINITY && !Kind.BLACKLIST.isTagged(block.type)) {
            block.breakNaturallyWithLog(player, player.inventory.itemInMainHand, true)
        }
    }

    private fun Block.canBreak(itemStack: ItemStack): Boolean {
        return blockData.lightEmission >= 1 && !(blockData.requiresCorrectToolForDrops() && !blockData.isPreferredTool(itemStack))
    }
}