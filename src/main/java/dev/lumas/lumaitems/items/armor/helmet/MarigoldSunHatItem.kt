package dev.lumas.lumaitems.items.armor.helmet

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.extensions.syncTimer
import dev.lumas.lumaitems.util.Tier
import kotlin.math.floor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack


class MarigoldSunHatItem : CustomItemFunctions() {


    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#F69A22:#E3A16B>Marig</gradient><gradient:#E3A16B:#A73F2B>old Sun hat</gradient></b>")
            .customEnchants("<#E3A16B>Drawing")
            .material(Material.NETHERITE_HELMET)
            .persistentData("marigold-sun-hat")
            .tier(Tier.HALLOWEEN_2025)
            .lore(
                "While wearing, nearby",
                "items in your vicinity",
                "will be drawn towards",
                "you over time.",
                "",
                "The brighter it is,",
                "the further this hat",
                "will reach."
            )
            .vanillaEnchants(
                Enchantment.PROTECTION to 5,
                Enchantment.UNBREAKING to 4,
                Enchantment.RESPIRATION to 3,
                Enchantment.AQUA_AFFINITY to 1,
                Enchantment.MENDING to 1
            )
            .buildPair()
    }


    override fun onAsyncRunnable(player: Player) {
        player.sync {
            val value = player.eyeLocation.block.lightLevel  / 1.76
            val roundedDown = floor(value * 2) / 2
            val range = roundedDown.coerceAtLeast(5.0)
            val items = player.location.getNearbyEntitiesByType(Item::class.java, range)
                .filter { player.location.distanceSquared(it.location) > 3.0 * 3.0 } // filter out items that are too close
            if (items.any { it.velocity.lengthSquared() > 0.1 }) {
                return@sync
            } // check if moving

            var count = 0
            items.syncTimer(0, 1) { task ->
                if (items.any { it.world != player.world } || ++count > 150) {
                    task.cancel()
                    return@syncTimer
                }

                items.forEach { it ->
                    val distance = player.eyeLocation.distanceSquared(it.location)
                    if (distance > 1.9 * 1.9) {
                        it.velocity = BukkitVectors.flyToLivingEntity(player, it, 2.0)
                    } else {
                        task.cancel()
                    }
                }
            }
        }
    }
}