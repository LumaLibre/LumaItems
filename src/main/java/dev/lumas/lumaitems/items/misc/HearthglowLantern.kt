package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.to

class HearthglowLantern : CustomItemFunctions() {

    companion object {
        private val KEY = Util.namespacedKey("hearthglow-lantern")
        private const val RANGE = 5.0
        private val GLOWING = PotionEffect(PotionEffectType.GLOWING, 32, 0, true, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#F6D6B8:#F1C48D:#F7B6A6:#E9B7D8>Hearthglow Lantern</gradient></b>")
            .customEnchants("<#F1C48D>Lumenwake")
            .material(Material.LANTERN)
            .persistentData(KEY)
            .tier(Tier.VALENTIDE_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .lore(
                "A lantern with a gentle",
                "and homey warmth.",
                "",
                "While held, nearby entities",
                "within <#F1C48D>5 blocks</#F1C48D> begin to",
                "<#F7B6A6>glow</#F7B6A6> softly."
            )
            .buildPair()
    }

    override fun onAsyncRunnable(player: Player) {
        if (!Util.isItemInSlot(KEY, EquipmentSlot.HAND, player) &&
            !Util.isItemInSlot(KEY, EquipmentSlot.OFF_HAND, player)) return
        player.location.sync {
            val nearby = player.getNearbyEntities(RANGE, RANGE, RANGE)
                .asSequence()
                .filterNot { it is Player && it.uniqueId == player.uniqueId } // optional: ignore self
                .filter { it.isValid }
                .toList()
            applyGlowing(nearby)
        }
    }

    private fun applyGlowing(entities: List<Entity>) {
        for (entity in entities) {
            entity.sync {
                if (!entity.isValid) return@sync
                val living = entity as? LivingEntity ?: return@sync
                living.addPotionEffect(GLOWING)
            }
        }
    }

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        if (!event.itemInHand.isMatchingItem(KEY)) return
        event.isCancelled = true
    }
}