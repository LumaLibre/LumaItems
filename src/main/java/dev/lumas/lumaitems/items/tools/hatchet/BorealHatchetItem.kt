package dev.lumas.lumaitems.items.tools.hatchet

import com.gmail.nossr50.events.skills.woodcutting.TreeFellerDestroyTreeEvent
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.syncDelayed
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.Tag
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class BorealHatchetItem : CustomItemFunctions() {

    companion object {
        private const val UNCOMPILED_LOG_PATTERN = "(LOG|WOOD|STEM|HYPHAE)"
        private val LOG_REPLACE_PATTERN = Regex("_$UNCOMPILED_LOG_PATTERN")
        private val LOG_MATCH_ANY_PATTERN = Regex(".*$UNCOMPILED_LOG_PATTERN")
        private val SOIL_MATCH_ANY_PATTERN = Regex(".*(MOSS|DIRT|MYCELIUM|PODZOL|GRASS_BLOCK|MUD|MUDDY_MANGROVE_ROOTS)")
        private const val REPLANT_DELAY_TICKS = 40L
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Boreal Hatchet")
            .material(Material.NETHERITE_AXE)
            .persistentData("boreal-hatchet")
            .vanillaEnchants(
                Enchantment.UNBREAKING to 4,
                Enchantment.EFFICIENCY to 7
            )
            .lore(
                "Automatically replants trees",
                "destroyed with Tree Feller,",
                "if you have saplings in",
                "your inventory for the",
                "respective wood type."
            )
            .buildPair()
    }


    override fun onMcMMOTreeFellerDestroyTree(player: Player, event: TreeFellerDestroyTreeEvent) {
        Executors.async {
            var factor = 0L
            for (block in event.blocks) {
                val below = block.getRelative(0, -1, 0)
                if (!LOG_MATCH_ANY_PATTERN.matches(block.type.name) || !SOIL_MATCH_ANY_PATTERN.matches(below.type.name)) {
                    continue
                }

                val saplingMaterial = saplingForWoodType(block.type) ?: continue
                if (!player.inventory.contains(saplingMaterial)) {
                    continue
                }
                factor += 5

                player.syncDelayed(REPLANT_DELAY_TICKS + factor) {
                    if (!player.inventory.contains(saplingMaterial)) {
                        return@syncDelayed
                    }

                    val saplingItem = ItemStack(saplingMaterial, 1)
                    player.inventory.removeItemAnySlot(saplingItem)

                    block.blockData = saplingMaterial.createBlockData()
                    val loc = block.location.toCenterLocation()
                    block.world.playSound(loc, Sound.ITEM_BOTTLE_FILL, 0.8f, 1.0f)
                    block.world.spawnParticle(Particle.DUST_PLUME, loc, 10, 0.4, 0.3, 0.4, 0.05)
                }
            }
        }
    }

    private fun saplingForWoodType(woodType: Material): Material? { // good enough
        val wood = woodType.name.replace(LOG_REPLACE_PATTERN, "")
        return Tag.SAPLINGS.values
            .firstOrNull { it.name.contains(wood) }
    }
}