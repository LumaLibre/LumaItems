package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.util.BukkitVectors
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.disabling.Ignore
import java.util.UUID
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDropItemEvent
import org.bukkit.inventory.ItemStack

@Ignore // TODO: Scrap
class BorealHatchetItemOld : CustomItemFunctions() {

    companion object {
        private val ITEM_BATCHES: MutableMap<UUID, MutableList<Item>> = mutableMapOf()
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Boreal Hatchet")
            .material(org.bukkit.Material.DIAMOND_AXE)
            .persistentData("boreal-hatchet")
            .buildPair()
    }

    override fun onBlockDropItem(player: Player, event: BlockDropItemEvent) {
        val items = ITEM_BATCHES.getOrPut(player.uniqueId) { mutableListOf() }
        items.addAll(event.items)
        ITEM_BATCHES[player.uniqueId] = items
    }



    override fun onRunnable(player: Player) {
        val items = ITEM_BATCHES.remove(player.uniqueId)
            ?.filterTo(mutableListOf()) { it.isValid }
            ?.ifEmpty { return }
            ?: return

        var count = 0
        Executors.syncTimer(0, 1) { task ->
            if (items.any { it.world != player.world } || items.isEmpty() || ++count > 50) {
                task.cancel()
                return@syncTimer
            }

            items.removeIf {
                if (it.world != player.world) {
                    task.cancel()
                    return@removeIf true
                }

                if (it.isValid) {
                    val distance = player.eyeLocation.distanceSquared(it.location)
                    if (distance > 1.9 * 1.9) {
                        it.velocity = BukkitVectors.flyToLivingEntity(player, it, 3.0, 0.2)
                        return@removeIf false
                    }
                }
                return@removeIf true
            }
        }
    }
}