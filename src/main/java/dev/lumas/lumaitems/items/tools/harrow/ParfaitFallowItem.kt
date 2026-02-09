package dev.lumas.lumaitems.items.tools.harrow

import dev.lumas.lumaitems.enums.CardinalDirection
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class ParfaitFallowItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#9b8a88:#fccec2:#fde4da:#FDF0DC>Parfait Fallow</gradient></b>")
            .customEnchants("<#FCCEC2>Spillover")
            .material(Material.NETHERITE_HOE)
            .persistentData("parfait-fallow")
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "When <#FCCEC2>breaking</#FCCEC2> crops,",
                "adjacent crops in the",
                "direction faced will",
                "also be broken.",
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 8,
                Enchantment.UNBREAKING to 9,
                Enchantment.FORTUNE to 4,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (!Tag.CROPS.isTagged(block.type)) {
            return
        }

        val direction = CardinalDirection.fromEntity(player)
        block.getRelative(direction.leftFace).breakNaturallyWithLog(player, player.inventory.itemInMainHand, true)
        block.getRelative(direction.rightFace).breakNaturallyWithLog(player, player.inventory.itemInMainHand, true)
    }
}