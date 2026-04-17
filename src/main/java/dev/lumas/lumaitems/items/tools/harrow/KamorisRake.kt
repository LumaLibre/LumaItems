package dev.lumas.lumaitems.items.tools.harrow

import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.particles.ParticleDisplay
import dev.lumas.lumaitems.particles.Particles
import dev.lumas.lumaitems.shapes.ShapeUtil
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.extensions.syncDelayed
import dev.lumas.lumaitems.util.extensions.toColor
import dev.lumas.lumaitems.util.tags.Kind
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.data.Ageable
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class KamorisRake : CustomItemFunctions() {

    private companion object {
        private const val KEY = "kamoris-rake"
        private val COLORS = listOf("#ebb2ff", "#FFC2E7", "#FAD4D4", "#CCF0FF", "#CCCCFF")
            .map { it.toColor() }
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ebb2ff:#FFC2E7:#FAD4D4:#CCF0FF:#CCCCFF>Kamori's Rake</gradient></b>")
            .customEnchants("<gradient:#FFC2E7:#CCCCFF>Reach","<gradient:#FFC2E7:#CCCCFF>Teru")
            .material(Material.NETHERITE_HOE)
            .tier(Tier.WONDERLAND_2026.alt())
            .persistentData(KEY)
            .attributeModifiers(
                AttributeContainer.of(KEY, Attribute.BLOCK_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, 2.0, EquipmentSlotGroup.MAINHAND),
            )
            .lore(
                "While <gradient:#FFC2E7:#CCCCFF>held</gradient>, you may",
                "reach up to 2 blocks",
                "further than normal.",
                "",
                "<gradient:#FFC2E7:#CCCCFF>Breaking</gradient> crops with",
                "this hoe may cause",
                "nearby crops to also",
                "automatically break.",
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 9,
                Enchantment.FORTUNE to 6,
                Enchantment.MENDING to 1,
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        if (!block.isTagged(Kind.CROPS) || random().nextDouble() > 0.1) {
            return
        }


        val blocks = ShapeUtil.circle(player.location.add(0.0, 0.2, 0.0), 5, 25)
            .filter { it != block && it.blockData is Ageable && (it.blockData as Ageable).age == (it.blockData as Ageable).maximumAge }
        val randomBlocks = blocks.shuffled().take(random().nextInt(1, 5))
        val particleDisplay = ParticleDisplay.of(Particle.DUST)
        val item = player.inventory.itemInMainHand

        for ((index, b) in randomBlocks.withIndex()) {
            b.syncDelayed(index * 5L) {
                b.breakNaturallyWithLog(player, item, true)
                item.damage(2, player)
                Particles.line(player.location.add(0.0,1.0,0.0), b.location, 0.2, particleDisplay.withColor(COLORS.random()))
            }
        }
    }
}