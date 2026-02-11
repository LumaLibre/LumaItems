package dev.lumas.lumaitems.items.tools.shears

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class IllumeShearsItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
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
                "<#c5adff>Left-click</#c5adff> to instantly",
                "break light-emitting",
                "blocks."
            )
            .buildPair()
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return

        if (block.blockData.lightEmission > 0 && block.getBreakSpeed(player) != Float.POSITIVE_INFINITY) {
            block.breakNaturallyWithLog(player, player.inventory.itemInMainHand, true)
        }
    }
}