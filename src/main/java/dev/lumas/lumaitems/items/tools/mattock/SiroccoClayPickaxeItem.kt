package dev.lumas.lumaitems.items.tools.mattock

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.itemInMainHand
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.inventory.ItemStack

class SiroccoClayPickaxeItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#555555:#c49e88:#daa4a4:#f2e2da>Sirocco Clay Pickaxe</gradient></b>")
            .customEnchants("<#daa4a4>Kaolin")
            .material(Material.DIAMOND_PICKAXE)
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
                "reinforced deepslate",
                "instantaneously."
            )
            .buildPair()
    }

    override fun onBlockDamage(player: Player, event: BlockDamageEvent) {
        if (event.block.type == Material.REINFORCED_DEEPSLATE) {
            event.instaBreak = true
            player.itemInMainHand.damage(10, player)
        }
    }
}