package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack

class CarnivalTargetPracticeBowItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#8EC4F7:#ff9ccb>Carn</gradient><gradient:#ff9ccb:#d7f58d>ival </gradient><gradient:#d7f58d:#fffe8a>Cros</gradient><gradient:#fffe8a:#ffd365>sBow</gradient></b>")
            .customEnchants(mutableListOf("<gray>Unbreakable"))
            .material(Material.CROSSBOW)
            .persistentData(mutableListOf("carnivaltargetpracticebow"))
            .tier(Tier.CARNIVAL_2024)
            .unbreakable(true)
            .vanillaEnchants(mutableMapOf(Enchantment.QUICK_CHARGE to 2, Enchantment.INFINITY to 1))
            .buildPair()
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        event.isCancelled = true
    }
}