package dev.lumas.lumaitems.items.misc.collectible

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

abstract class PrideItem : CustomItemFunctions() {

    val baseItem: ItemFactory.Builder = ItemFactory.builder()
        .hideEnchants(true)
        .vanillaEnchants(Enchantment.UNBREAKING to 1)
        .tier(Tier.PRIDE_2025)
}

class PrideMazeTokenItem : PrideItem() {

    override fun createItem(): Pair<String, ItemStack> {
        return baseItem
            .name("<b><gradient:#ff6666:#ffbd55:#ffff66:#9de24f:#87cefa>Pride Maze Token</gradient></b>")
            .lore(
                "<gray>A token given to those",
                "<gray>who have completed the",
                "<gray>Pride 2025 maze.",
            )
            .material(Material.FLINT)
            .persistentData("pride-maze-token")
            .buildPair()
    }
}

class PrideTokenItem : PrideItem() {

    override fun createItem(): Pair<String, ItemStack> {
        return baseItem
            .name("<b><gradient:#ff6666:#ffbd55:#ffff66:#9de24f:#87cefa>Pride Token</gradient></b>")
            .lore(
                "<gray>Currency used in the",
                "<gray>Pride 2025 event."
            )
            .material(Material.QUARTZ)
            .persistentData("pride-token")
            .buildPair()
    }
}

class RadiantCharmItem : PrideItem() {

    companion object {
        private val REGENERATION = PotionEffect(PotionEffectType.REGENERATION, 200, 1, true, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return baseItem
            .name("<b><#FF63B1>R<#EA4292>a<#D52172>d<#C00053>i<#D50E7C>a<#EA1CA6>n<#FF2ACF>t <#CF4EDE>C<#B760E6>h<#CB42BA>a<#DE248D>r<#F20661>m")
            .lore(
                "<gray>A small, intracately carved",
                "<gray>charm radiating strength,",
                "<gray>glowing faintly with pride."
            )
            .material(Material.AMETHYST_SHARD)
            .persistentData("radiant-charm")
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        if (QuickTasks.isOnCooldown(this, player.uniqueId)) {
            return
        }
        QuickTasks.addCooldown(this, player.uniqueId, 600)
        player.addPotionEffect(REGENERATION)
    }

}

class PrideShardLanternItem : PrideItem() {

    companion object {
        private val NIGHT_VISION = PotionEffect(PotionEffectType.NIGHT_VISION, 300, 0, true, false, false)
        private val key = Util.namespacedKey("pride-shard-lantern")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return baseItem
            .name("<b><#D76249>P<#DF714B>r<#E7804E>i<#EE9050>d<#F69F53>e <#FEBB56>S<#FDC757>h<#FDD458>a<#FCE059>r<#FBCA57>d <#F89F53>L<#F68951>a<#F5734F>n<#F8965B>t<#FAB966>e<#FDDC72>r<#FFFF7D>n")
            .lore(
                "<gray>A beautifully faceted lantern",
                "<gray>containing a shard of pure",
                "<gray>light, casting gentle, vibrant",
                "<gray>colors around it."
            )
            .material(Material.LANTERN)
            .persistentData(key)
            .buildPair()
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (!item.hasItemMeta()) return
        if (Util.hasPersistentKey(item.itemMeta, key)) {
            event.isCancelled = true
        }
    }

    override fun onRunnable(player: Player) {
        player.addPotionEffect(NIGHT_VISION)
    }
}