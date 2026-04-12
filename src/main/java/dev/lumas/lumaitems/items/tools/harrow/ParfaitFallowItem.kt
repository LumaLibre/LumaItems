package dev.lumas.lumaitems.items.tools.harrow

import com.gmail.nossr50.events.skills.secondaryabilities.SubSkillBlockEvent
import dev.lumas.lumaitems.enums.CardinalDirection
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Kind
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.takeItem
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
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
        if (!Kind.CROPS.isTagged(block.type)) {
            return
        }

        val direction = CardinalDirection.fromEntity(player)
        val block1 = block.getRelative(direction.leftFace)
        val block2 = block.getRelative(direction.rightFace)
        val item = player.inventory.itemInMainHand
        if (block1.type == block.type) {
            block1.breakNaturallyWithLog(player, item, true)
        }
        if (block2.type == block.type) {
            block2.breakNaturallyWithLog(player, item, true)
        }
    }

    override fun onMcMMOHerbalismReplant(player: Player, event: SubSkillBlockEvent) {
        val direction = CardinalDirection.fromEntity(player)
        val block = event.block

        val block1 = block.getRelative(direction.leftFace)
        val block2 = block.getRelative(direction.rightFace)

        val type = block.type

        block.syncDelayed(20) {
            val itemType = block.state.drops.firstOrNull()?.type ?: return@syncDelayed
            if (player.takeItem(ItemStack.of(itemType, 2))) {
                if (block1.type.isAir) block1.type = type
                if (block2.type.isAir) block2.type = type
            }
        }

    }
}