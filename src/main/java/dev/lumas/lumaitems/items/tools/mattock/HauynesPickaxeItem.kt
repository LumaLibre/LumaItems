package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.itemStack
import dev.lumas.lumaitems.util.tags.LinkedTags
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class HauynesPickaxeItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ab92fb:#c2b1f9:#c9fbff:#aad7e1:#8098ec>Hauyne's Pickaxe</gradient></b>")
            .customEnchants("<gradient:#c2b1f9:#aad7e1>Tepid")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData("hauynes-pickaxe")
            .tier(Tier.WONDERLAND_2026.alt())
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 8,
                Enchantment.FORTUNE to 6,
                Enchantment.UNBREAKING to 4,
                Enchantment.MENDING to 1
            )
            .lore(
                "Automatically smelts",
                "<gradient:#c2b1f9:#aad7e1>broken</gradient> ores."
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        event.isDropItems = false
        val item = player.inventory.itemInMainHand
        val block = event.block

        if (!LinkedTags.SMELTABLE_ORES.hasKey(block.type)) {
            return
        }

        val drops = if (block.type != Material.NETHER_GOLD_ORE) block.getDrops(item, player)
            .associate { it.type to it.amount } else mapOf(Material.NETHER_GOLD_ORE to 1)

        val loc = block.location.toCenterLocation()
        for (drop in drops) {
            val converted = LinkedTags.SMELTABLE_ORES.get(drop.key)?.itemStack(drop.value) ?: continue
            block.world.dropItem(loc, converted)
        }

        block.world.spawnParticle(Particle.DUST_PLUME, loc, 5, 0.4, 0.3, 0.4, 0.05)
    }
}