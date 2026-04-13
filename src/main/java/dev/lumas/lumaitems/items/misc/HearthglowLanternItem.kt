package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class HearthglowLanternItem : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("hearthglow-lantern")
        private const val RANGE = 5.0
        private val GLOWING = PotionEffect(PotionEffectType.GLOWING, 32, 0, true, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#F6D6B8:#F1C48D:#F7B6A6:#E9B7D8>Hearthglow Lantern</gradient></b>")
            .customEnchants("<#F1C48D>Lumenwake")
            .material(Material.COPPER_LANTERN)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .lore(
                "A lantern with a gentle",
                "and homey warmth.",
                "",
                "<#F1C48D>While held</#F1C48D>, nearby entities",
                "within 5 blocks begin to",
                "glow softly."
            )
            .buildPair()
    }

    override fun onAsyncRunnable(player: Player) {
        player.location.sync {
            val nearby = player.getNearbyEntities(RANGE, RANGE, RANGE)
                .filterNot { it is Player }.mapNotNull { it as? LivingEntity }
                .filterNot { it.type == EntityType.ARMOR_STAND } // furniture
                .toList()
            applyGlowing(nearby)
        }
    }

    private fun applyGlowing(entities: List<LivingEntity>) {
        for (entity in entities) {
            entity.sync {
                entity.addPotionEffect(GLOWING)
            }
        }
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        if (!event.itemInHand.isMatchingItem(KEY)) return
        event.isCancelled = true
    }
}