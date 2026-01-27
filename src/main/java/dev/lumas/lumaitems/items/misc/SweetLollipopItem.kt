package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.ItemUtil.isMatchingItem
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SweetLollipopItem : CustomItemFunctions() {

    companion object {
        private val HASTE = PotionEffect(PotionEffectType.HASTE, 260, 1, false, false, false)
        private val KEY = Util.namespacedKey("sweet-lollipop")
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#ffc8de:#d4b1ff:#ffa2df:#ff92d9:#ee35ae>Sweet Lollipop</gradient></b>")
            .customEnchants("<#d4b1ff>Haste II")
            .material(Material.ALLIUM)
            .persistentData(KEY)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "A lollipop so sweet,",
                "it feels like a sugar",
                "cloud exploded in your",
                "mouth."
            )
            .buildPair()
    }

    override fun onRunnable(player: Player) {
        player.addPotionEffect(HASTE)
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (item.isMatchingItem(KEY)) {
            event.isCancelled = true
        }
    }
    
    fun playerDeath(event: PlayerDeathEvent) {
        event.damageSource.damageType.deathMessageType
    }
}