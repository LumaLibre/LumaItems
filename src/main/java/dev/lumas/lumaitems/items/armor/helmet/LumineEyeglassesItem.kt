package dev.lumas.lumaitems.items.armor.helmet

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import dev.lumas.lumaitems.util.Executors.sync
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class LumineEyeglassesItem : CustomItem {


    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#717FEF&lL&#7D84F1&lu&#8889F2&lm&#948EF4&li&#9F93F5&ln&#AB98F7&le &#B49BF8&lE&#BE9FF9&ly&#C7A2F9&le&#D1A6FA&lg&#DAA9FB&ll&#D8A1F6&la&#D798F1&ls&#D590ED&ls&#D487E8&le&#D27FE3&ls",
            mutableListOf("&#AB98F7Magnetic"),
            mutableListOf("While wearing, nearby items", "will slowly be pulled closer", "in your direction."),
            Material.NETHERITE_HELMET,
            mutableListOf("lumineeyeglasses"),
            mutableMapOf(Enchantment.PROTECTION to 7, Enchantment.UNBREAKING to 8, Enchantment.BLAST_PROTECTION to 6, Enchantment.MENDING to 1)
        )
        item.addQuote("&#AB98F7\"&#AD97F6W&#AF96F5a&#B095F4i&#B293F3t&#B492F2, &#B691F2y&#B790F1o&#B98FF0u &#BB8EEFw&#BD8DEEe&#BF8CEDa&#C08AECr &#C289EBg&#C488EAl&#C687E9a&#C786E8s&#C985E8s&#CB84E7e&#CD82E6s&#CE81E5?&#D080E4!&#D27FE3\"")
        item.tier = "&#FF9A9A&lE&#FFBAA6&la&#FFD9B2&ls&#FFF9BE&lt&#E5FAD4&le&#CAFCE9&lr &#B0FDFF&l2&#C7E8FF&l0&#DED4FF&l2&#F5BFFF&l4"

        return Pair("lumineeyeglasses", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.ASYNC_RUNNABLE -> {
                player.sync {
                    val originLocation = player.eyeLocation
                    val nearbyItems = player.location.world?.getNearbyEntities(player.location, 8.5, 8.5, 8.5)
                        ?.filterIsInstance<Item>() ?: return@sync

                    for (item in nearbyItems) {
                        val direction: Vector = originLocation.clone().subtract(item.location).toVector()
                        val distance: Double = direction.x + direction.y + direction.z


                        if (distance > 0.7 || distance < -0.7) {
                            item.velocity = direction.normalize().multiply(0.5)
                        }
                    }
                }
            }
            else -> return false
        }
        return true
    }
}