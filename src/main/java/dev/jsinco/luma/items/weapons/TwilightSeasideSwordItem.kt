package dev.jsinco.luma.items.weapons

import dev.jsinco.luma.items.ItemFactory
import dev.jsinco.luma.enums.Action
import dev.jsinco.luma.manager.CustomItem
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Enemy
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class TwilightSeasideSwordItem : CustomItem {

    companion object {
        private val speed = PotionEffect(PotionEffectType.SPEED, 100, 0, true, false, false)
        private val haste = PotionEffect(PotionEffectType.HASTE, 100, 0, true, false, false)
        private val wither = PotionEffect(PotionEffectType.WITHER, 100, 0, true, false, false)
    }

    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#BA7EEE&lT&#B382ED&lw&#AC85ED&li&#A589EC&ll&#9E8CEC&li&#9790EB&lg&#9093EB&lh&#8997EA&lt&#8AA3EC&l &#8CAEEE&lS&#8DBAF0&le&#8FC5F2&la&#90D1F4&ls&#92DCF6&li&#93E8F8&ld&#9FD7F1&le&#ACC6EA&l &#B8B5E3&lS&#C4A5DD&lw&#D094D6&lo&#DD83CF&lr&#E972C8&ld",
            mutableListOf("&#8FC5F2Swift Surge"),
            mutableListOf("Hitting enemies with this weapon", "will offer speed and haste buffs", "while also inflicting withering."),
            Material.NETHERITE_SWORD,
            mutableListOf("twilightseasidesword"),
            mutableMapOf(Enchantment.MENDING to 1, Enchantment.UNBREAKING to 10, Enchantment.SHARPNESS to 8, Enchantment.LOOTING to 4, Enchantment.SWEEPING_EDGE to 3, Enchantment.FIRE_ASPECT to 3)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        return Pair("twilightseasidesword", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.ENTITY_DAMAGE -> {
                event as EntityDamageByEntityEvent
                val entity: Enemy = event.entity as? Enemy ?: return false
                player.addPotionEffect(speed)
                player.addPotionEffect(haste)
                entity.addPotionEffect(wither)
            }
            else -> return false
        }
        return true
    }

}
