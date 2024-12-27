package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.manager.CustomItem
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Particle.DustOptions
import org.bukkit.Sound
import org.bukkit.Statistic
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerFishEvent
import org.bukkit.inventory.ItemStack
import java.util.Random
import kotlin.math.max

class SeasideSurfRodItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#EFB7AE&lS&#EDBEB1&le&#EAC4B5&la&#E8CBB8&ls&#E0CCBE&li&#D9CCC4&ld&#D1CDCA&le &#BAD2DA&lS&#AFD4E2&lu&#A6D0DB&lr&#9ECDD4&lf &#8BC1C5&lR&#81BABE&lo&#77B2B6&ld",
            mutableListOf("&#95c9cdSunside Bets"),
            mutableListOf("While fishing with this rod, you will","have the chance to catch a surplus","of fish or lose your catch.","","Your chances of wagering and a","favorable gamble increase with the","amount of fish you have caught."),
            Material.FISHING_ROD,
            mutableListOf("seasidesurfrod"),
            mutableMapOf(Enchantment.LURE to 5, Enchantment.LUCK_OF_THE_SEA to 5, Enchantment.UNBREAKING to 9, Enchantment.MENDING to 1)
        )
        item.tier = "&#F34848&lS&#E36643&lo&#D3843E&ll&#C3A239&ls&#B3C034&lt&#A3DE2F&li&#93FC2A&lc&#7DE548&le&#66CD66&l &#50B684&l2&#399EA1&l0&#2387BF&l2&#0C6FDD&l4"
        return Pair("seasidesurfrod", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {

        when (type) {
            Action.FISH -> {
                event as PlayerFishEvent
                if (event.state == PlayerFishEvent.State.CAUGHT_FISH) {
                    wager(player, event.caught as Item)
                }
            }
            else -> return false
        }
        return true
    }

    // if someone reaches int limit change to long ig
    private fun wager(player: Player, caught: Item) {
        val chance: Int =  player.getStatistic(Statistic.FISH_CAUGHT) / 1000
        if (Random().nextInt(100) > max((12 + chance).toDouble(), 20.0)) return  // chance to wager is 12% + 1% per 1000 fish caught, max 20%

        // 70% base chance + 1% per 1000 fish caught
        if (Random().nextInt(100) <= 70 + chance) {
            caught.world.spawnParticle(
                Particle.DUST, caught.location.add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.1, DustOptions(
                    Color.fromRGB(176, 140, 253), 2f
                )
            )
            caught.world.spawnParticle(
                Particle.DUST, caught.location.add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.1, DustOptions(
                    Color.fromRGB(189, 244, 251), 2f
                )
            )
            caught.world.playSound(caught.location, Sound.ENTITY_FIREWORK_ROCKET_LARGE_BLAST_FAR, 1f, 1f)
            if (caught.itemStack.getMaxStackSize() > 1) caught.itemStack.amount =
                Random().nextInt(2, 5) // set amount to 2-4
        } else {
            caught.world.spawnParticle(
                Particle.DUST, caught.location.add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.1, DustOptions(
                    Color.fromRGB(255, 10, 10), 2f
                )
            )
            caught.world.spawnParticle(Particle.WITCH, caught.location.add(0.0, 1.0, 0.0), 15, 0.5, 0.5, 0.5, 0.1)
            caught.world.playSound(caught.location, Sound.ENTITY_WITCH_CELEBRATE, 0.5f, 1f)
            caught.remove()
        }
    }
}