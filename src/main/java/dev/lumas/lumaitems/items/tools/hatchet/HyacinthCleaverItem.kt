package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack


class HyacinthCleaverItem : CustomItemFunctions() {

    private companion object {
        private val CHARCOAL = ItemStack.of(Material.CHARCOAL)
        private val PARTICLE_DATA = Particle.DustOptions(Color.BLACK, 1f)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#dda1de:#d8bfd9:#ffc0cb:#ffb6c2:#db7094>Hyacinth Cleaver</gradient></b>")
            .customEnchants("<#dda1de>Bloom")
            .persistentData("hyacinth-cleaver")
            .material(Material.NETHERITE_AXE)
            .tier(Tier.VALENTIDE_2026)
            .tagline("#dda1de", "Got any flowers?")
            .lore(
                "When <#dda1de>breaking</#dda1de> logs,",
                "this axe will instantly",
                "transform them into",
                "charcoal."
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 6,
                Enchantment.UNBREAKING to 4,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (!Tag.LOGS.isTagged(block.type)) {
            return
        }

        // Should this payout for LJ?
//        event.isCancelled = true
//        block.setAirWithLog(player)
        event.isDropItems = false

        val loc = block.location.toCenterLocation()
        block.world.dropItemNaturally(loc, CHARCOAL)

        Executors.async {
            block.world.spawnParticle(Particle.DUST, loc, 10, 0.4, 0.4, 0.4, 0.1, PARTICLE_DATA)
        }
    }
}