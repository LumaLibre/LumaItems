package dev.lumas.lumaitems.items.misc.builderswand

import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.canBuild
import dev.lumas.lumaitems.util.extensions.flagFor
import dev.lumas.lumaitems.util.extensions.isFlagged
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.extensions.syncTimer
import io.canvasmc.canvas.event.EntityTeleportAsyncEvent
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack

class ArchitectsCompassItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#FC8EAC:#7FFFD4:#FF5C00>Architect's Compass</gradient></b>")
            .customEnchants("<#FC8EAC>Concentric")
            .material(Material.COMPASS)
            .persistentData(ARCHITECTS_COMPASS_KEY, BUILDERS_WAND_ADDON_KEY)
            .tier(Tier.LUMARINE_2026)
            .tagline("#FC8EAC", "Draw the perfect ring!")
            .vanillaEnchants(Enchantment.UNBREAKING to 10, Enchantment.KNOCKBACK to 2)
            .lore(
                "Hold in your <#FC8EAC>off-hand</#FC8EAC>",
                "beside a <#CDA9FF>Builder's Wand</#CDA9FF>",
                "to draw circles instead",
                "of paths.",
                "",
                "<#FC8EAC>Right-click</#FC8EAC> a block to",
                "center a circle on you,",
                "then look outward to set",
                "the radius. <#FC8EAC>Left-click</#FC8EAC>",
                "to confirm or press your",
                "<#FC8EAC>swap key (F)</#FC8EAC> to cancel.",
                "",
                "<red>Cooldown: 11s"
            )
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (!player.isBuildersWandAddon()) return

        val clickedBlock = event.clickedBlock ?: return
        val clickedFace = event.blockFace
        val relativeBlock = clickedBlock.getRelative(clickedFace)
        event.isCancelled = true

        if (!player.canBuild(clickedBlock.location) || player.isOnCooldown(this) || WAND_BLACKLIST.invoke(clickedBlock)) {
            relativeBlock.location.toCenterLocation().spawnWandDust()
            return
        }

        val visualizer = CircleVisualizer(player, relativeBlock, clickedBlock, this)
        visualizer.start(clickedFace)
        WandVisualizers.put(player, visualizer)
    }

    override fun onLeftClick(player: Player, event: PlayerInteractEvent) {
        if (!player.isBuildersWandAddon()) return

        event.isCancelled = true
        if (player.isFlagged(this)) return
        player.flagFor(this, 1)

        val visualizer = WandVisualizers.take(player) ?: return
        commitWandBuild(visualizer, player, 20L * 11)
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        WandVisualizers.take(player)?.let { event.isCancelled = true }
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        WandVisualizers.take(player)
    }

    override fun onPlayerTeleport(player: Player, event: PlayerTeleportEvent) {
        WandVisualizers.take(player)
    }

    override fun onCanvasAsyncPlayerTeleport(player: Player, event: EntityTeleportAsyncEvent) {
        WandVisualizers.take(player)
    }

    override fun onPluginDisable(player: Player) {
        WandVisualizers.take(player)
    }


    private class CircleVisualizer(
        player: Player,
        pos: Block,
        origin: Block,
        item: CustomItem,
        private val maxRadius: Int = 22,
        private val reachLength: Int = 22
    ) : WandVisualizer(player, origin, item) {

        private val world: World = pos.world
        private val centerX: Int = pos.x
        private val centerY: Int = pos.y
        private val centerZ: Int = pos.z
        private var lastRadius: Int = -1

        override fun isStillEquipped(player: Player) = player.isBuildersWandAddon()

        override fun start(face: BlockFace) {
            task = player.syncTimer(0, 3) {
                val rayResult = player.rayTraceBlocks(reachLength.toDouble()) ?: return@syncTimer
                val target = rayResult.hitBlock ?: return@syncTimer

                // Radius is the horizontal distance from the center to the block the player is looking at.
                val dx = target.x - centerX
                val dz = target.z - centerZ
                val radius = min(sqrt((dx * dx + dz * dz).toDouble()).roundToInt(), maxRadius)

                if (radius == lastRadius) return@syncTimer
                lastRadius = radius

                val locations = circlePoints(centerX, centerZ, radius).mapNotNull { (x, z) ->
                    val loc = Location(world, x + 0.5, centerY + 0.5, z + 0.5)
                    if (loc.block.isReplaceable) loc else null
                }

                lastPath = locations
                render(locations)
            }
        }

        private fun circlePoints(cx: Int, cz: Int, radius: Int): Set<Pair<Int, Int>> {
            val points = HashSet<Pair<Int, Int>>()
            if (radius <= 0) {
                points.add(cx to cz)
                return points
            }

            var x = radius
            var z = 0
            var err = 1 - radius

            while (x >= z) {
                points.add(cx + x to cz + z)
                points.add(cx - x to cz + z)
                points.add(cx + x to cz - z)
                points.add(cx - x to cz - z)
                points.add(cx + z to cz + x)
                points.add(cx - z to cz + x)
                points.add(cx + z to cz - x)
                points.add(cx - z to cz - x)

                z++
                if (err < 0) {
                    err += 2 * z + 1
                } else {
                    x--
                    err += 2 * (z - x) + 1
                }
            }
            return points
        }
    }
}
