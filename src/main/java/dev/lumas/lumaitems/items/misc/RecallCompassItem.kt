package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class RecallCompassItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "recall-compass".namespacedKey()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#F6C1D1:#E8B7E8:#D7C2F2:#F4B6C2>Recall Compass</gradient></b>")
            .customEnchants("<#D7C2F2>Attunement")
            .lore(
                "A compass humming with",
                "soft magical energy.",
                "",
                "<#D7C2F2>Sneak</#D7C2F2> + <#D7C2F2>right-click</#D7C2F2>",
                "to recall to your",
                "spawn point."
            )
            .tier(Tier.VALENTIDE_2026)
            .persistentData(KEY)
            .material(Material.COMPASS)
            .vanillaEnchants(Enchantment.LOYALTY to 10)
            .hideEnchants(true)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        event.item?.takeIf { it.isMatchingItem(KEY) } ?: return

        if (event.clickedBlock?.type == Material.LODESTONE) {
            player.send("<#D7C2F2>This compass only knows one destination... Home.")
            event.isCancelled = true
            return
        }

        if (event.hand != EquipmentSlot.HAND || !player.isSneaking || player.isOnCooldown(this)) return


        val from = player.location
        val to = player.respawnLocation ?: run {
            player.send("<#D7C2F2>You have no respawn point set.")
            return
        }

        if (from.world.equals(to.world) && from.distanceSquared(to) < 9) {
            player.send("<#D7C2F2>You are already within your comfort zone.")
            return
        }

        spawnRecallParticles(from)
        from.world.playSound(from, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f)
        player.addCooldown(this, 50)

        player.teleportAsync(to).thenAccept { success ->
            if (!success) return@thenAccept
            spawnRecallParticles(to)
            to.sync {
                to.world.playSound(to, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f)
            }
        }
    }

    private fun spawnRecallParticles(loc: Location) {
        val world = loc.world ?: return
        val center = loc.clone().add(0.0, 0.25, 0.0)
        world.spawnParticle(Particle.PORTAL, center, 80, 0.4, 0.6, 0.4, 0.05)
        world.spawnParticle(Particle.ENCHANT, center, 40, 0.3, 0.5, 0.3, 0.0)
    }

}