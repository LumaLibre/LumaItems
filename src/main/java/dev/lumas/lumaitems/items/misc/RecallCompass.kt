package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.MiniMessageUtil
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.sync
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class RecallCompass : CustomItemFunctions() {

    private val key = "recall-compass"

    override fun createItem(): Pair<String, ItemStack> {

        return ItemFactory.builder()
            .name("<b><gradient:#F6C1D1:#E8B7E8:#D7C2F2:#F4B6C2>Recall Compass</gradient></b>")
            .customEnchants("<#D7C2F2>Attunement")
            .lore(
                "A compass humming with",
                "soft magical energy.",
                "",
                "<#D7C2F2>Shift + Right-Click</#D7C2F2>",
                "to recall to your",
                "spawnpoint."
            )
            .tier(Tier.VALENTIDE_2026)
            .persistentData(key)
            .material(Material.COMPASS)
            .vanillaEnchants(Enchantment.LOYALTY to 10)
            .hideEnchants(true)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (isLodestoneInteraction(player, event)) {
            MiniMessageUtil.msg(player, "<#D7C2F2>This compass only knows one destination. Home.")
            event.isCancelled = true
            return
        }

        if (event.hand != EquipmentSlot.HAND) return
        if (!player.isSneaking) return

        val item = event.item ?: return
        if (!item.isMatchingItem(key)) {
            return
        }

        val from = player.location.clone()
        val to = player.respawnLocation ?: player.world.spawnLocation

        if (from.world.equals(to.world) && from.distanceSquared(to) < 9) {
            MiniMessageUtil.msg(player, "<#D7C2F2>You are already within your comfort zone.")
            return
        }

        from.sync {
            spawnRecallParticles(from)
            from.world.playSound(from, Sound.BLOCK_BEACON_DEACTIVATE, 1.0f, 1.0f)
        }

        player.teleportAsync(to).thenAccept { success ->
            if (!success) return@thenAccept
            to.sync {
                spawnRecallParticles(to)
                to.world.playSound(to, Sound.BLOCK_BEACON_POWER_SELECT, 1.0f, 1.0f)
            }
        }
    }

    private fun isLodestoneInteraction(player: Player, event: PlayerInteractEvent): Boolean {
        val item = event.item ?: return false
        if (!item.isMatchingItem(key)) return false
        return event.clickedBlock?.type == Material.LODESTONE
    }

    private fun spawnRecallParticles(loc: Location) {
        val world = loc.world ?: return
        val center = loc.clone().add(0.0, 0.25, 0.0)
        world.spawnParticle(Particle.PORTAL, center, 80, 0.4, 0.6, 0.4, 0.05)
        world.spawnParticle(Particle.ENCHANT, center, 40, 0.3, 0.5, 0.3, 0.0)
    }

}