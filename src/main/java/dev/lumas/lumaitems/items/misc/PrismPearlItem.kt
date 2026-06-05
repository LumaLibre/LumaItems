package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.actionBar
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.canDamage
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.namespacedKey
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Animals
import org.bukkit.entity.Enemy
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class PrismPearlItem : CustomItemFunctions() {

    private companion object {
        private val KEY = "prism-pearl".namespacedKey()
        private const val SEARCH_RADIUS = 40.0
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#ff3b3b:#ff8848:#feff5d:#aaff59:#84ffd7>Prism Pearl</gradient></b>")
            .material(Material.ENDER_PEARL)
            .persistentData(KEY)
            .tier(Tier.PRIDE_2026)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "A pearl with a strange",
                "<#feff5d>connection</#feff5d> to passive",
                "creatures.",
                "",
                "Right-click to <#feff5d>summon</#feff5d>",
                "the nearest passive",
                "mob to your side.",
                "",
                "<red>5s Cooldown"
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (player.isOnCooldown(this)) return

        val item = player.inventory.itemInMainHand
        if (!item.isMatchingItem(KEY)) return

        event.isCancelled = true

        val nearestMob = player.world.getNearbyEntities(
            player.location, SEARCH_RADIUS, SEARCH_RADIUS, SEARCH_RADIUS
        )
            .filter { it is Animals && it !is Enemy && player.canDamage(it) }
            .minByOrNull { it.location.distanceSquared(player.location) }

        if (nearestMob == null) {
            player.actionBar("<red>No passive mobs nearby!")
            return
        }

        nearestMob.teleportAsync(player.location)
        player.addCooldown(this, 100)
        player.world.playSound(player.location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)

        val mobName = nearestMob.type.name.lowercase().replaceFirstChar { it.uppercase() }
        player.actionBar("<gradient:#ff8848:#aaff59:#feff5d:#84ffd7>You summoned a $mobName!</gradient>")
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val item = player.inventory.itemInMainHand
        if (!item.isMatchingItem(KEY)) return

        event.isCancelled = true
    }
}