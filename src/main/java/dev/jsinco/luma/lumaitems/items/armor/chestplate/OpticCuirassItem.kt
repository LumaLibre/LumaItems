package dev.jsinco.luma.lumaitems.items.armor.chestplate

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType


class OpticCuirassItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#6b89f6&lO&#6e93f6&lp&#719df6&lt&#74a7f6&li&#77b1f6&lc &#7abbf6&lC&#7cc4f6&lu&#7fcef6&li&#82d8f6&lr&#85e2f6&la&#88ecf6&ls&#8bf6f6&ls",
            mutableListOf("&#084cfbH&#1655fao&#245dfat&#3266f9-&#416ff8S&#4f78f7w&#5d80f7a&#6b89f6p"),
            mutableListOf("&#6a87f3\"&#6a88f3T&#6b8af3h&#6b8bf3e&#6c8df3r&#6c8ef3e&#6d90f3'&#6d91f3s &#6e93f3a &#6e94f3m&#6e96f3a&#6f97f3r&#6f99f3k&#709af3i&#709cf3n&#719df3g &#719ff3o&#72a0f3n &#72a2f3t&#73a3f3h&#73a5f3e &#73a6f3c&#74a8f3e&#74a9f3n&#75abf3t&#75acf3e&#76aef3r &#76aff3t&#77b1f3h&#77b2f3a&#77b4f3t","&#78b5f3l&#78b7f3o&#79b8f3o&#79baf3k&#7abcf3s &#7abdf3l&#7bbff3i&#7bc1f3k&#7cc3f3e &#7cc4f3a&#7dc6f3n &#7dc8f3e&#7ec9f3y&#7ecbf3e&#7ecdf3, &#7fcff3I &#7fd0f3w&#80d2f3o&#80d4f3n&#81d6f3d&#81d7f3e&#82d9f3r &#82dbf3w&#83dcf3h&#83def3a&#84e0f3t &#84e2f3i&#85e3f3t&#85e5f3'&#86e7f3s &#86e9f3f&#87eaf3o&#87ecf3r&#88eef3.&#88f0f3.&#89f1f3.&#89f3f3\"","","§fThis armor gives night vision and","§fresistance to the wearer.","","§fPress your swap key while holding to","§fswap armor types"),
            Material.NETHERITE_CHESTPLATE,
            mutableListOf("opticcuirass"),
            mutableMapOf(Enchantment.PROTECTION to 7, Enchantment.BLAST_PROTECTION to 9, Enchantment.FEATHER_FALLING to 4, Enchantment.UNBREAKING to 8, Enchantment.MENDING to 1)
        )
        return Pair("opticcuirass", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val playerSwapHands: PlayerSwapHandItemsEvent? = event as? PlayerSwapHandItemsEvent

        when (type) {
            Action.SWAP_HAND -> {
                swapArmor(playerSwapHands?.offHandItem ?: return false, player)
                playerSwapHands.isCancelled = true
            }
            Action.RUNNABLE -> {
                opticEffects(player)
            }
            else -> return false
        }
        return true
    }

    private fun swapArmor(opticCuirass: ItemStack, player: Player) {
        when (opticCuirass.type) {
            Material.NETHERITE_CHESTPLATE -> opticCuirass.type = Material.NETHERITE_LEGGINGS
            Material.NETHERITE_LEGGINGS -> opticCuirass.type = Material.NETHERITE_BOOTS
            Material.NETHERITE_BOOTS -> opticCuirass.type = Material.NETHERITE_HELMET
            Material.NETHERITE_HELMET -> opticCuirass.type = Material.NETHERITE_CHESTPLATE
            Material.LEATHER_CHESTPLATE -> opticCuirass.type = Material.LEATHER_LEGGINGS
            Material.LEATHER_LEGGINGS -> opticCuirass.type = Material.LEATHER_BOOTS
            Material.LEATHER_BOOTS -> opticCuirass.type = Material.LEATHER_HELMET
            Material.LEATHER_HELMET -> opticCuirass.type = Material.LEATHER_CHESTPLATE
            else -> opticCuirass.type = Material.NETHERITE_CHESTPLATE
        }
        player.inventory.setItemInMainHand(opticCuirass)
    }

    private fun opticEffects(player: Player) {
        player.addPotionEffect(PotionEffect(PotionEffectType.NIGHT_VISION, 220, 0, false, false, false))
        player.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 220, 2, false, false, false))
    }
}