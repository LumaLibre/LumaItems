package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.PaperDataComponent
import dev.lumas.lumaitems.util.Tier
import io.papermc.paper.datacomponent.DataComponentTypes
import kotlin.jvm.optionals.getOrNull
import net.minecraft.world.level.block.WeatheringCopper
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.craftbukkit.block.CraftBlock
import org.bukkit.craftbukkit.block.data.CraftBlockData
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class CinnamonOxidizerItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#8a2c13:#bb566f:#ebc07d:#e39932:#bc7209:#774b03>Cinnamon Oxidizer</gradient></b>")
            .customEnchants("<#e39932>Weatherer")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData("cinnamon-oxidizer")
            .tier(Tier.CHRISTMAS_2025)
            .paperDataComponents(
                PaperDataComponent.valued(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("copper_pickaxe"))
            )
            .lore(
                "A mattock with cinnamon-",
                "like coating on the edges",
                "of it. It looks so good",
                "you're inclined to take",
                "a bite.",
                "",
                "When <#e39932>breaking</#e39932> any kind",
                "of unwaxed copper block,",
                "this tool will speed up",
                "the weathering process",
                "to the next stage.",
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 7,
                Enchantment.FORTUNE to 4,
                Enchantment.UNBREAKING to 9,
                Enchantment.MENDING to 1,
                Enchantment.SMITE to 10,
                Enchantment.KNOCKBACK to 2
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val bukkitBlock = event.block
        val craftBlock = bukkitBlock as CraftBlock
        val block = WeatheringCopper.getNext(craftBlock.nms.block).getOrNull() ?: return

        // back to bukkit
        val newBlockData = CraftBlockData.fromData(block.defaultBlockState())

        bukkitBlock.blockData = newBlockData
        bukkitBlock.world.spawnParticle(Particle.DUST_PLUME, bukkitBlock.location.toCenterLocation(), 6, 0.3, 0.3, 0.3, 0.0)
    }
}