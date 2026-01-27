package dev.lumas.lumaitems.items.tools.harrow

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.BlockUtil.relatives
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

class SnowflakeHarrowerItem : CustomItemFunctions() {

    companion object  {
        // TODO: Replace with Tag#DIRT?
        private val SOIL_PATTERN = Regex(".*DIRT*.|GRASS_BLOCK")
        private val FARMLAND = Material.FARMLAND.createBlockData()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#6F79B0:#8ECCEA:#ECB7C0:#B89ACD>Snowflake Harrower</gradient></b>")
            .customEnchants("<#8ECCEA>Pattern Tiller")
            .material(Material.NETHERITE_HOE)
            .persistentData("snowflake-harrower")
            .tier(Tier.CHRISTMAS_2025)
            .lore(
                "<#8ECCEA>Right-click</#8ECCEA> to plow",
                "any variant of dirt",
                "in a cross shaped",
                "pattern."
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 7,
                Enchantment.UNBREAKING to 6,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()

    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val clicked = event.clickedBlock ?: return

        if (!clicked.canTill()) {
            return
        }

        for (block in clicked.relatives(BlockFace.UP, BlockFace.DOWN)) {
            if (block.canTill()) {
                block.blockData = FARMLAND
            }
        }
    }


    private fun Block.canTill(): Boolean {
        return SOIL_PATTERN.matches(this.type.name) && this.getRelative(BlockFace.UP).isEmpty
    }

}