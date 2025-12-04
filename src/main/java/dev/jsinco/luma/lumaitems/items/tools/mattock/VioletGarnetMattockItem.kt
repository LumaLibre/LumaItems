package dev.jsinco.luma.lumaitems.items.tools.mattock

import dev.jsinco.luma.lumaitems.enums.BlockConstants
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.shapes.Sphere
import dev.jsinco.luma.lumaitems.util.extensions.BlockUtil
import dev.jsinco.luma.lumaitems.util.extensions.BlockUtil.breakNaturallyWithLog
import dev.jsinco.luma.lumaitems.util.extensions.BlockUtil.determineHighestBreakSpeed
import dev.jsinco.luma.lumaitems.util.extensions.BlockUtil.setAirWithLog
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class VioletGarnetMattockItem : CustomItemFunctions() {

    companion object {
        // lots of constants for this one
        private val AMETHYST_CLUSTER_PATTERN = Regex(".*_AMETHYST_BUD|AMETHYST_CLUSTER")
        private val AMETHYST_BLOCK_DATA = Material.AMETHYST_CLUSTER.createBlockData()
        private val BUDDING_AMETHYST_ITEMSTACK = ItemStack.of(Material.BUDDING_AMETHYST)
        private val COMPARABLE_BREAKSPEED_ITEMSTACKS = arrayOf(
            ItemStack.of(Material.NETHERITE_PICKAXE),
            ItemStack.of(Material.NETHERITE_SHOVEL),
            ItemStack.of(Material.NETHERITE_AXE),
            ItemStack.of(Material.SHEARS),
            ItemStack.of(Material.NETHERITE_HOE)
        )
        private val EXCLUDED_COMPARABLE_BREAKSPEED_MATERIALS = setOf(
            Material.GRAVEL, Material.DIRT, Material.ROOTED_DIRT, Material.CLAY
        )
    }


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#5757B3:#728fd6:#76A7C3:#B3ABF6:#937DD4>Violet Garnet Mattock</gradient></b>")
            .customEnchants("<#937DD4>Amethyst Breaker", "<#B3ABF6>Forgiving Touch")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData("violet-garnet-mattock")
            .tier(Tier.CHRISTMAS_2025)
            .lore(
                "<#937DD4>Breaking</#937DD4> amethyst clusters",
                "or buds with this mattock",
                "will cause all blocks in a",
                "large radius to also break.",
                "",
                "Budding amethyst will drop",
                "as items when <#B3ABF6>broken</#B3ABF6> with",
                "this tool."
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 7,
                Enchantment.UNBREAKING to 9,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val hitBlock = event.block

        if (hitBlock.type == Material.BUDDING_AMETHYST) {
            hitBlock.dropBudding()
            return
        } else if (!AMETHYST_CLUSTER_PATTERN.matches(hitBlock.type.name) && !hitBlock.checkRelatives()) {
            return
        }

        val sphere = Sphere(hitBlock.location, 3.0, 0.0)
        val item = player.inventory.itemInMainHand

        sphere.sphereFast
            .filter { block ->
                val bestTool = block.determineHighestBreakSpeed(*COMPARABLE_BREAKSPEED_ITEMSTACKS)
                return@filter item.type == bestTool.type || EXCLUDED_COMPARABLE_BREAKSPEED_MATERIALS.contains(block.type)
            }
            .forEach { block ->
                if (BlockConstants.BLACKLISTED.contains(block.type) || !block.isSolid) {
                    return@forEach
                }

                if (!AMETHYST_CLUSTER_PATTERN.matches(block.type.name)) {
                    if (block.type == Material.BUDDING_AMETHYST) {
                        block.dropBudding()
                    }
                    block.breakNaturallyWithLog(player, item)
                } else {
                    block.setAirWithLog(player) // no drops for amethyst clusters/buds
                }

                block.world.spawnParticle(Particle.BLOCK, block.location.toCenterLocation(), 1, 0.5, 0.5, 0.5, 0.1, AMETHYST_BLOCK_DATA)
                player.damageItemStack(item, 1)
            }
    }

    private fun Block.checkRelatives(): Boolean {
        for (face in BlockUtil.BLOCK_FACE_RELATIVES) {
            val relative = this.getRelative(face)
            if (AMETHYST_CLUSTER_PATTERN.matches(relative.type.name)) {
                return true
            }
        }
        return false
    }

    private fun Block.dropBudding() {
        this.world.spawnParticle(Particle.DUST_PLUME, this.location.toCenterLocation(), 10, 0.5, 0.5, 0.5, 0.1)
        this.world.dropItemNaturally(this.location, BUDDING_AMETHYST_ITEMSTACK)
    }
}