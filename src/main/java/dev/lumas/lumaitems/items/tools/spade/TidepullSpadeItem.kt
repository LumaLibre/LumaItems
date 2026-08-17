package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.sync
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.ItemStack

class TidepullSpadeItem : CustomItemFunctions() {

    private companion object {
        val KEY = "tidepull-spade".namespacedKey()
        val COLLECTIBLE_SAND = setOf(Material.SAND, Material.RED_SAND)

        const val CHORUS_CHANCE = 0.01
        const val CHORUS_RADIUS = 4
        const val CHORUS_ATTEMPTS = 8
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#1e8abf:#9be4df:#ffe494>Tidepull Spade</gradient></b>")
            .customEnchants("<#9be4df>Undertow")
            .material(Material.NETHERITE_SHOVEL)
            .persistentData(KEY)
            .tier(Tier.LUMARINE_2026)
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 5,
                Enchantment.UNBREAKING to 5,
                Enchantment.MENDING to 1,
            )
            .lore(
                "<#9be4df>Sand</#9be4df> broken with this",
                "spade is swept straight",
                "into your inventory.",
                "",
                "but beware, theres a",
                "chance you get swept",
                "away instead!",
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val block = event.block
        val type = block.type
        if (!COLLECTIBLE_SAND.contains(type)) return

        event.isDropItems = false

        val result = player.give(listOf(ItemStack(type)), false)

        if (result.leftovers().isEmpty()) {
            block.world.spawnParticle(Particle.PORTAL, block.location.toCenterLocation(), 12, 0.25, 0.25, 0.25, 0.05)
            block.world.playSound(block.location, Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.6f)
        } else {
            for (leftover in result.leftovers()) {
                block.world.dropItemNaturally(block.location.toCenterLocation(), leftover)
            }
        }

        maybeChorusTeleport(player)
    }

    private fun maybeChorusTeleport(player: Player) {
        if (Random.nextDouble() > CHORUS_CHANCE) return

        val origin = player.location
        repeat(CHORUS_ATTEMPTS) {
            val dx = Random.nextInt(-CHORUS_RADIUS, CHORUS_RADIUS + 1)
            val dz = Random.nextInt(-CHORUS_RADIUS, CHORUS_RADIUS + 1)
            if (dx == 0 && dz == 0) return@repeat
            val dy = Random.nextInt(-1, 2)

            val destination = origin.clone().add(dx.toDouble(), dy.toDouble(), dz.toDouble())
            if (!destination.isChorusSafe()) return@repeat

            playTeleportEffect(origin)
            player.teleportAsync(destination).thenAccept { success ->
                if (!success) return@thenAccept
                destination.sync {
                    playTeleportEffect(destination)
                }
            }
            return
        }
    }

    private fun playTeleportEffect(location: Location) {
        val world = location.world ?: return
        val column = location.clone().add(0.0, 1.0, 0.0)

        world.playSound(location, Sound.BLOCK_PORTAL_TRAVEL, 1f, 1f)
        world.playSound(location, Sound.ENTITY_ENDERMAN_TELEPORT, 1f, 1f)
        world.spawnParticle(Particle.PORTAL, column, 120, 0.4, 1.0, 0.4, 0.6)
        world.spawnParticle(Particle.REVERSE_PORTAL, column, 60, 0.4, 1.0, 0.4, 0.3)
    }

    private fun Location.isChorusSafe(): Boolean {
        val world = this.world ?: return false
        val feet = world.getBlockAt(this)
        val head = feet.getRelative(BlockFace.UP)
        return !feet.type.isSolid && !head.type.isSolid
    }
}
