package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.itemInMainHand
import dev.lumas.lumaitems.util.extensions.itemStack
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.inventory.ItemStack

class SiroccoClayPickaxeItem : CustomItemFunctions() {

    private companion object {
        private val REINFORCED_DEEPSLATE = Material.REINFORCED_DEEPSLATE.itemStack()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#555555:#c49e88:#daa4a4:#f2e2da>Sirocco Clay Pickaxe</gradient></b>")
            .customEnchants("<#daa4a4>Kaolin")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData("sirocco-clay-pickaxe")
            .tier(Tier.WONDERLAND_2026)
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 4,
                Enchantment.UNBREAKING to 4,
                Enchantment.MENDING to 1
            )
            .lore(
                "A pickaxe made of a",
                "special hardened clay",
                "capable of <#daa4a4>destroying</#daa4a4>",
                "and dropping reinforced",
                "deepslate instantly."
            )
            .buildPair()
    }

    override fun onBlockDamage(player: Player, event: BlockDamageEvent) {
        val block = event.block
        if (block.type == Material.REINFORCED_DEEPSLATE) {
            event.instaBreak = true
            block.world.dropItem(block.location.toCenterLocation(), REINFORCED_DEEPSLATE)
            player.itemInMainHand.damage(10, player)
        }
    }
}