package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.enums.Action
import dev.lumas.lumaitems.manager.CustomItem
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack

class CozyCampFireItem : CustomItem {
    override fun createItem(): Pair<String, ItemStack> {
        val item = ItemFactory(
            "&#fb5151&lE&#fb664c&lm&#fb7a46&lb&#fb8f41&le&#fba33b&lr",
            mutableListOf("&#fb941aC&#fbab20o&#fbc326z&#fbda2cy"),
            mutableListOf("§fHolding this campfire will","§fheal you and nearby players"),
            Material.CAMPFIRE,
            mutableListOf("cozycampfire"),
            mutableMapOf(Enchantment.INFINITY to 1)
        )
        return Pair("cozycampfire", item.createItem())
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        val blockPlaceEvent: BlockPlaceEvent? = event as? BlockPlaceEvent

        when (type) {
            Action.RUNNABLE -> {
                cozy(player)
            }
            Action.PLACE_BLOCK -> {
                blockPlaceEvent!!.isCancelled = true
            }
            else -> return false
        }
        return true
    }

    private fun cozy(p: Player) {
        healPlayer(p)
        p.getNearbyEntities(10.0, 10.0, 10.0).forEach { entity ->
            if (entity !is Player || entity.gameMode == GameMode.SPECTATOR || entity.hasMetadata("vanished")) return@forEach
            healPlayer(entity)
        }
    }

    private fun healPlayer(player: Player) { // LOL this code fucking sucks, 10/10/2023, I'm a better developer nowadays
        player.world.spawnParticle(Particle.HEART, player.location.add(0.0, 1.0, 0.0), 5, 0.5, 0.5, 0.5, 0.1)
        for (i in 0 until player.health.toInt()) {
            try {
                if (player.health == 20.0 || i == 5) break
                player.health++
            } catch (exception: IllegalArgumentException) {
                break
            }
        }
    }
}