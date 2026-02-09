package dev.lumas.lumaitems.items.armor.helmet

import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class OGNeptunesCrownItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "OG-&#006cd0&lN&#0c76cf&le&#1780ce&lp&#238acd&lt&#2e94cc&lu&#3a9ecb&ln&#45a8ca&le&#53acc3&l'&#63aab4&ls &#73a8a5&lC&#82a796&lr&#92a587&lo&#a2a378&lw&#b2a169&ln",
            mutableListOf("&#006cd0Olympian"),
            mutableListOf("§fWearing this helmet will grant you", "§fConduit power and Dolphin's grace"),
            Material.NETHERITE_HELMET,
            mutableListOf("neptunescrown"),
            mutableMapOf(
                Enchantment.PROTECTION to 6,
                Enchantment.FIRE_PROTECTION to 10,
                Enchantment.UNBREAKING to 5,
                Enchantment.RESPIRATION to 4,
                Enchantment.AQUA_AFFINITY to 1,
                Enchantment.MENDING to 1
            )
        )
        return Pair("neptunescrown", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                player.addPotionEffect(PotionEffect(PotionEffectType.DOLPHINS_GRACE, 220, 0, false, false, false))
            }
            else -> return false
        }
        return true
    }
}