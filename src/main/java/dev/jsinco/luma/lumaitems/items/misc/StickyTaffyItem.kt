package dev.jsinco.luma.lumaitems.items.misc

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import java.util.IdentityHashMap
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.ShulkerBox
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerAttemptPickupItemEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.BlockStateMeta

class StickyTaffyItem : CustomItemFunctions() {

    companion object {
        private val SHULKER_PATTERN = Regex(".*SHULKER_BOX")
        private const val MAX_SHULKERS = 1
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><gradient:#f9e1f5:#c8d1ff:#d3caff:#dbecb1:#ffd6a5>Sticky Taffy</gradient></b>")
            .customEnchants("<#d3caff>Sticky")
            .material(Material.MAGENTA_DYE)
            .persistentData("sticky-taffy")
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "A sticky taffy that can",
                "be held to store items",
                "in a singular shulker",
                "box when your inventory",
                "is full."
            )
            .buildPair()
    }

    override fun onPlayerPickupItem(player: Player, event: PlayerAttemptPickupItemEvent) {
        if(event.isCancelled) return
        val physicalItem = event.item
        val picked = physicalItem.itemStack.clone()
        if (picked.amount > 64) {
            throw IllegalStateException("item amount should not be greater than 64")
        }

        if (event.remaining != picked.amount || isShulkerBox(picked.type)) return

        val shulkers = getShulkers(player.inventory).ifEmpty {
            return
        }

        // Sort shulkers by how full they are
        val sorted = shulkers.entries
            .sortedByDescending { (_, meta) ->
                val box = meta.blockState as? ShulkerBox
                box?.inventory?.contents?.filterNotNull()?.sumOf { it.amount } ?: 0
            }
            .take(MAX_SHULKERS) // only top X

        // check if all are full
        if (sorted.all { (_, meta) ->
                val box = meta.blockState as? ShulkerBox
                val inv = box?.inventory ?: run {
                    throw IllegalStateException("BlockState in shulker meta is not a ShulkerBox")
                }
                // maybe remove second half of this check, or use maxStackSizes instead?
                cannotHold(inv , picked) || inv.filterNotNull().sumOf { it.amount } >= 64 * 27
            }) {
            // just exit if all are full
            return
        }

        var leftOver: ItemStack? = picked
        for (entry in sorted) {
            val blockStateMeta = entry.value
            val blockState = blockStateMeta.blockState as? ShulkerBox ?: continue


            val itemStack = (leftOver ?: break).clone()
            leftOver = blockState.inventory.addItem(itemStack).values.takeIf {
                if (it.size > 1) throw IllegalStateException("remaining itemStacks should not be >1") else true
            }?.firstOrNull()

            blockStateMeta.blockState = blockState
            entry.key.itemMeta = blockStateMeta
        }


        if (leftOver == null || leftOver.amount != physicalItem.itemStack.amount) {
            physicalItem.world.playSound(physicalItem.location, Sound.ENTITY_ITEM_PICKUP, 0.8f, 1.9f)
            if (leftOver != null) {
                physicalItem.itemStack = leftOver
            } else {
                physicalItem.remove()
            }
        }
    }

    private fun getShulkers(playerInventory: PlayerInventory): Map<ItemStack, BlockStateMeta> {
        val shulkers = IdentityHashMap<ItemStack, BlockStateMeta>()
        playerInventory.contents.filterNotNull().forEach {
            if (isShulkerBox(it.type)) {
                shulkers[it] = (it.itemMeta as? BlockStateMeta ?: return@forEach)
            }
        }
        return shulkers
    }

    private fun cannotHold(inventory: Inventory, itemStack: ItemStack): Boolean {
        return inventory.firstEmpty() < 0 && inventory.first(itemStack.type) < 0
    }

    private fun isShulkerBox(material: Material): Boolean = material.name.matches(SHULKER_PATTERN)

}