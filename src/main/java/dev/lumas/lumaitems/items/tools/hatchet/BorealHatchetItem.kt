package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.extensions.itemStack
import dev.lumas.lumaitems.util.extensions.setBlockDataWithLog
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.tags.Kind
import dev.lumas.lumaitems.util.tags.LinkedTags
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class BorealHatchetItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#2D7263:#679a77:#753b55:#662440>Boreal Hatchet</gradient></b>")
            .customEnchants("<#679a77>Shrub")
            .material(Material.NETHERITE_AXE)
            .tier(Tier.WONDERLAND_2026.alt())
            .persistentData("boreal-hatchet")
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 10,
                Enchantment.FORTUNE to 5,
                Enchantment.UNBREAKING to 10,
                Enchantment.MENDING to 1
            )
            .lore(
                "A hatchet with the",
                "ability to replant",
                "broken logs.",
                "",
                "Upon <#679a77>breaking</#679a77> a log,",
                "this axe will attempt",
                "to replant a sapling",
                "of the same type."
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val brokenBlock = event.block.takeIf { Tag.LOGS.isTagged(it.type) } ?: return

        if (!brokenBlock.getRelative(BlockFace.DOWN).isTagged(Kind.SAPLING_GROWABLE)) {
            return
        }

        val sapling = LinkedTags.LOG_TO_SAPLING.get(brokenBlock.type) ?: return

        if (!player.inventory.contains(sapling)) {
            return
        }

        brokenBlock.syncDelayed(40) {
            val saplingItem = sapling.itemStack()
            player.inventory.removeItemAnySlot(saplingItem)

            brokenBlock.setBlockDataWithLog(player, sapling)

            val loc = brokenBlock.location.toCenterLocation()
            loc.world.playSound(loc, Sound.ITEM_BOTTLE_FILL, 0.8f, 1.0f)
            loc.world.spawnParticle(Particle.DUST_PLUME, loc, 10, 0.4, 0.3, 0.4, 0.05)
        }
    }
}