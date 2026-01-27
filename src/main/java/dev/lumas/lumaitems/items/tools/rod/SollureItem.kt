package dev.lumas.lumaitems.items.tools.rod

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

class SollureItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#7290fb&lS&#869bf3&lo&#9ba6eb&ll&#afb1e3&ll&#c3bcda&lu&#d8c7d2&lr&#ecd2ca&le",
            mutableListOf(
                "&7Unbreakable",
                "&#a2bbfbE&#b2c0f0n&#c3c5e5c&#d3cadah&#e4cfcfa&#e8cec3n&#e1c6b6t&#dabea8i&#d3b69bn&#ccae8dg"
            ),
            mutableListOf(
                "§fHolding this rod will give you Luck II",
                "",
                "§fFishing with this rod will grant you double",
                "§fthe fish"
            ),
            Material.FISHING_ROD,
            mutableListOf("sollure"),
            mutableMapOf(Enchantment.LURE to 6, Enchantment.LUCK_OF_THE_SEA to 6)
        )
        item.unbreakable = true
        return Pair("sollure", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val fishEvent: PlayerFishEvent? = event as? PlayerFishEvent
        when (type) {
            Action.RUNNABLE -> {
                player.addPotionEffect(PotionEffect(PotionEffectType.LUCK, 220, 1, false, false, false))
            }

            Action.FISH -> {
                if (fishEvent!!.state == PlayerFishEvent.State.CAUGHT_FISH) {
                    sollureFish(fishEvent.hook, fishEvent.caught!!, false)
                } else if (fishEvent.state == PlayerFishEvent.State.FISHING) {
                    sollureFish(fishEvent.hook, null, true)
                }

            }

            else -> return false
        }
        return true
    }

    private fun sollureFish(hook: Entity, caught: Entity?, onlyCasting: Boolean) {
        hook.isGlowing = true
        if (onlyCasting) return
        hook.world.spawnParticle(Particle.WAX_OFF, hook.location.add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.1)
        val item = caught as Item
        if (item.itemStack.getMaxStackSize() > 1) item.itemStack.amount = 2
        item.isGlowing = true
    }
}