package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.PersistentDataRecord
import dev.lumas.lumaitems.util.Kind
import dev.lumas.lumaitems.util.extensions.random
import dev.lumas.lumaitems.util.extensions.spell
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class RainierCherryMattockItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#cb354e:#ff7d7c:#ffa5a5:#ffdca4:#ffb47b:#fe9164>Rainier Cherry Mattock</gradient></b>")
            .customEnchants("<#ffa5a5>Canvas")
            .persistentData("rainier-cherry-mattock")
            .material(Material.NETHERITE_PICKAXE)
            .persistentDataRecords(PersistentDataRecord.PREVENT_NETHERITE_SMITHING)
            .tagline("#ffa5a5", "Color perfectionist!")
            .tier(Tier.VALENTIDE_2026)
            .lore(
                "<#ffa5a5>Breaking</#ffa5a5> stones with this",
                "mattock will yield several",
                "different variations of",
                "terracotta.",
                "",
                "<#ffa5a5>Break</#ffa5a5> nether stones to",
                "yield glazed terracotta."
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 6,
                Enchantment.UNBREAKING to 4,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
//            .paperDataComponents(
//                PaperDataComponent.valued(DataComponentTypes.SWING_ANIMATION)
//                    .value(
//                        SwingAnimation.swingAnimation()
//                            .type(SwingAnimation.Animation.STAB)
//                            .duration(15)
//                            .build()
//                    )
//            )
            .buildPair()
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        val type = block.type

        val dropTag = when {
            Tag.BASE_STONE_OVERWORLD.isTagged(type) -> Tag.TERRACOTTA
            Tag.BASE_STONE_NETHER.isTagged(type) -> Kind.GLAZED_TERRACOTTA
            else -> return
        }

        event.isDropItems = false
        val center = block.location.toCenterLocation()
        val selected = dropTag.random()
        block.world.dropItemNaturally(center, ItemStack.of(selected))
        selected.createBlockData().mapColor.let { color ->
            block.world.spawnParticle(Particle.INSTANT_EFFECT, center, 3, 0.5, 0.5, 0.5, 0.1, color.spell())
        }
    }

}