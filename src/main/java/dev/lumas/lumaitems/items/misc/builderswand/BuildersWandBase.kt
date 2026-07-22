package dev.lumas.lumaitems.items.misc.builderswand

import com.destroystokyo.paper.MaterialTags
import dev.lumas.lumaitems.model.item.CustomItem
import dev.lumas.lumaitems.model.task.Synchronizable
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.canBuild
import dev.lumas.lumaitems.util.extensions.dustOptions
import dev.lumas.lumaitems.util.extensions.isItemInSlot
import dev.lumas.lumaitems.util.extensions.isTagged
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.setBlockDataWithLog
import dev.lumas.lumaitems.util.extensions.takeItem
import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import java.util.concurrent.ConcurrentHashMap
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.event.inventory.InventoryType
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.type.Chest
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Leaves
import org.bukkit.block.data.type.Piston
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.joml.AxisAngle4f
import org.joml.Vector3f

/**
 * Shared utilities between the [BuildersWandItem] and its off-hand add-ons.
 */

internal val BUILDERS_WAND_KEY = "builders-wand".namespacedKey()
internal val BUILDERS_WAND_ADDON_KEY = "builders-wand-addon".namespacedKey()
internal val ARCHITECTS_COMPASS_KEY = "architects-compass".namespacedKey()

private val WAND_RED_DUST = "#EA6363".dustOptions()

internal val WAND_BLACKLIST: (Block) -> Boolean = { block ->
    val blockData = block.blockData
    block.type.isTagged(MaterialTags.ORES) || blockData is Chest || blockData is Ageable ||
        blockData is Door || blockData is Piston || !blockData.material.isItem
}

internal fun Player.isBuildersWandAddon(): Boolean =
    isItemInSlot(BUILDERS_WAND_KEY, EquipmentSlot.HAND) &&
        isItemInSlot(BUILDERS_WAND_ADDON_KEY, EquipmentSlot.OFF_HAND)

internal fun Player.isArchitectsCompassCombo(): Boolean =
    isItemInSlot(BUILDERS_WAND_KEY, EquipmentSlot.HAND) &&
        isItemInSlot(ARCHITECTS_COMPASS_KEY, EquipmentSlot.OFF_HAND)

internal fun Location.spawnWandDust() =
    world.spawnParticle(Particle.DUST, this, 5, 0.35, 0.35, 0.35, WAND_RED_DUST)

internal fun commitWandBuild(visualizer: WandVisualizer, player: Player, cooldownTicks: Long) {
    val path = visualizer.path
    val blockData = visualizer.block.blockData.apply {
        if (this is Leaves) isPersistent = true
    }

    val material = blockData.material
    val doPlace = player.gameMode == GameMode.CREATIVE ||
        (material.isItem && player.takeItem(ItemStack.of(material, path.size)))

    for (loc in path) {
        if (doPlace) {
            if (!player.canBuild(loc)) break
            loc.block.setBlockDataWithLog(player, blockData)
        } else {
            loc.spawnWandDust()
        }
    }

    if (doPlace) {
        player.addCooldown(visualizer.item, cooldownTicks)
    }
}

abstract class WandVisualizer(
    protected val player: Player,
    protected val origin: Block,
    val item: CustomItem
) : Synchronizable.Block {

    companion object {
        private val TRANSFORMATION = Transformation(
            Vector3f(-0.25f, -0.25f, -0.25f),
            AxisAngle4f(),
            Vector3f(0.5f, 0.5f, 0.5f),
            AxisAngle4f()
        )
    }

    final override val block: Block = origin

    protected val activeDisplays = mutableListOf<BlockDisplay>()
    protected var task: ScheduledTask? = null
    protected var lastPath: List<Location> = emptyList()

    val path: List<Location> get() = lastPath

    abstract fun start(face: BlockFace)

    /** Whether [player] is still holding the tool(s) this preview belongs to. */
    abstract fun isStillEquipped(player: Player): Boolean

    protected fun render(renderPath: Collection<Location>) {
        val newSet = renderPath.toSet()
        val oldSet = activeDisplays.associateBy { it.location.toCenterLocation() }

        val toRemove = oldSet.keys - newSet
        for (loc in toRemove) {
            oldSet[loc]?.remove()
        }

        val kept = oldSet.filterKeys { it in newSet }
        activeDisplays.clear()
        activeDisplays.addAll(kept.values)

        val toAdd = newSet - oldSet.keys
        for (point in toAdd) {
            val display = player.world.spawn(point, BlockDisplay::class.java) {
                it.isPersistent = false
                it.block = origin.blockData
                it.transformation = TRANSFORMATION
            }
            activeDisplays.add(display)
        }
    }

    fun stop() {
        task?.cancel()
        activeDisplays.forEach { it.remove() }
        this.syncDelayed(4) {
            activeDisplays.forEach { it.remove() }
            activeDisplays.clear()
        }
    }
}

internal object WandVisualizers {

    private val ALLOWED_INV_TYPES = setOf(InventoryType.CRAFTING, InventoryType.PLAYER, InventoryType.CREATIVE)
    private val active = ConcurrentHashMap<Player, WandVisualizer>()

    fun put(player: Player, visualizer: WandVisualizer) {
        active.put(player, visualizer)?.stop()
    }

    fun take(player: Player): WandVisualizer? = active.remove(player)?.also { it.stop() }

    fun cleanupInvalid() {
        for ((player, visualizer) in active) {
            if (!ALLOWED_INV_TYPES.contains(player.openInventory.type) || !visualizer.isStillEquipped(player)) {
                active.remove(player)?.let { v -> v.sync { v.stop() } }
            }
        }
    }
}
