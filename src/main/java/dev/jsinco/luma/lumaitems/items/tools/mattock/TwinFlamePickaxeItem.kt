package dev.jsinco.luma.lumaitems.items.tools.mattock

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack

class TwinFlamePickaxeItem : CustomItemFunctions() {

    private val key = Util.namespacedKey("twin-flame-pickaxe")

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><#FF4B91>T<#FF5A9D>w<#FF6AA8>i<#FF89C0>n <#FFA8D7>F<#FF98CB>l<#FFA8D7>a<#E2AEE5>m<#C9B4F2>e <#B5BEFF>P<#C9B4F2>i<#E2AEE5>c<#FFA8D7>k<#FF98CB>a<#FF6AA8>x<#FF4B91>e")
            .customEnchants("<#FF6AA8>Harmony")
            .quotes("<gradient:#FF6AA8:#FFA8D7>\"One heart, two souls.\"")
            .material(Material.NETHERITE_PICKAXE)
            .lore(
                "Press your <#FFA8D7>swap key (F)",
                "to switch between Silk Touch",
                "and Fortune."
            )
            .tier(Tier.VALENTIDE_2025)
            .persistentData(key.key)
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 7,
                Enchantment.FIRE_ASPECT to 6,
                Enchantment.FORTUNE to 5,
                Enchantment.UNBREAKING to 8,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        val item = player.inventory.itemInMainHand
        val meta = item.itemMeta ?: return
        if (!meta.persistentDataContainer.has(key)) {
            return
        }

        if (meta.hasEnchant(Enchantment.FORTUNE)) {
            meta.removeEnchant(Enchantment.FORTUNE)
            meta.addEnchant(Enchantment.SILK_TOUCH, 1, true)
            player.sendActionBar(MiniMessageUtil.mm("<#FF6AA8>Switched to Silk Touch"))
        } else if (meta.hasEnchant(Enchantment.SILK_TOUCH)) {
            meta.removeEnchant(Enchantment.SILK_TOUCH)
            meta.addEnchant(Enchantment.FORTUNE, 5, true)
            player.sendActionBar(MiniMessageUtil.mm("<#FF6AA8>Switched to Fortune"))
        }
        item.itemMeta = meta
        event.isCancelled = true
    }
}