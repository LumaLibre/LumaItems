package dev.lumas.lumaitems.items.armor.boots

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import kotlin.math.min
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.inventory.ItemStack

class UnnamedBootsItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gray>Unnamed Boots</gray></b>")
            .material(org.bukkit.Material.LEATHER_BOOTS)
            .persistentData("unnamed-boots")
            .buildPair()
    }

    override fun onPlayerDamaged(player: Player, event: EntityDamageEvent) {
        if (event.cause != EntityDamageEvent.DamageCause.FALL) {
            return
        }

        val fallDistance = min(player.fallDistance.toDouble(), 50.0)
        val vectory = BukkitVectors.bounceWithBlockFace(player, BlockFace.UP, fallDistance)
        player.velocity = vectory.multiply(0.50)
        event.isCancelled = true
    }
}