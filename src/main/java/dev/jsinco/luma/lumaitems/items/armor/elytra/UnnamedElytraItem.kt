package dev.jsinco.luma.lumaitems.items.armor.elytra

import com.destroystokyo.paper.event.player.PlayerJumpEvent
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.AbilityUtil.isLocationOnGround
import dev.jsinco.luma.lumaitems.util.BukkitVectors
import dev.jsinco.luma.lumaitems.util.QuickTasks
import java.util.UUID
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInputEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class UnnamedElytraItem : CustomItemFunctions() {

    companion object {
        private val REFERENCES: MutableMap<UUID, Vector> = HashMap()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("unnamed elytra")
            .material(Material.ELYTRA)
            .persistentData("unnamed-elytra")
            .buildPair()
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (player.isLocationOnGround(0.5) && !player.isFlying && !player.isGliding && !player.isSneaking) {
            REFERENCES[player.uniqueId] = event.to.clone().subtract(event.from.clone()).toVector()
        } else if (REFERENCES.containsKey(player.uniqueId)) {
            REFERENCES.remove(player.uniqueId)
        }
    }


    override fun onPlayerCrouch(player: Player, event: PlayerToggleSneakEvent) {
        if (!event.isSneaking || !REFERENCES.contains(player.uniqueId) || QuickTasks.isOnCooldown(this, player)) return
        val direction = REFERENCES.remove(player.uniqueId) ?: return

        QuickTasks.addCooldown(this, player, 18)
        player.velocity = direction.normalize().multiply(7.0).setY(-0.1)
    }

}