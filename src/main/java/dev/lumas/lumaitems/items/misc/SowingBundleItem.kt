package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.extensions.actionBar
import dev.lumas.lumaitems.util.extensions.addCooldown
import dev.lumas.lumaitems.util.extensions.computeDyedBundleResult
import dev.lumas.lumaitems.util.extensions.isOnCooldown
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.block.Action as BukkitAction
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BundleMeta

class SowingBundleItem : CustomItemFunctions() {

    companion object {
        private const val KEY = "sowing-bundle"

        private val SEED_TO_CROP = mapOf(
            Material.WHEAT_SEEDS to Material.WHEAT,
            Material.CARROT to Material.CARROTS,
            Material.POTATO to Material.POTATOES,
            Material.BEETROOT_SEEDS to Material.BEETROOTS,
            Material.TORCHFLOWER_SEEDS to Material.TORCHFLOWER,
            Material.PITCHER_POD to Material.PITCHER_CROP,
            Material.PUMPKIN_SEEDS to Material.PUMPKIN_STEM,
            Material.MELON_SEEDS to Material.MELON_STEM,
        )

        private val ADJACENT_FACES = listOf(BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST)
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#7ABF6A:#C2A05C:#E8B84B>Sowing Bundle</gradient></b>")
            .customEnchants("<gradient:#7ABF6A:#E8B84B>Broadcast Sow</gradient>")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .hideEnchants(true)
            .lore(
                "Seeds spill faster than",
                "fingers can follow.",
                "",
                "<#B1BB57>Right-click</#B1BB57> farmland while",
                "holding seeds inside to sow",
                "the surrounding farmland.",
                "",
                "<red>Cooldown: 1s per seed sown"
            )
            .material(Material.BROWN_BUNDLE)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (event.hand != EquipmentSlot.HAND) return
        if (event.action != BukkitAction.RIGHT_CLICK_BLOCK) return

        val clickedBlock = event.clickedBlock ?: return
        if (clickedBlock.type != Material.FARMLAND) {
            // Event is fired multiple times for the main hand. This is needed.
            if (clickedBlock.getRelative(BlockFace.DOWN).type == Material.FARMLAND) {
                event.setUseInteractedBlock(Event.Result.DENY)
                event.setUseItemInHand(Event.Result.DENY)
            }
            return
        }

        event.setUseInteractedBlock(Event.Result.DENY)
        event.setUseItemInHand(Event.Result.DENY)

        val item = event.item ?: return
        val bundleMeta = item.itemMeta as? BundleMeta ?: return
        val contents = bundleMeta.items.toMutableList()

        if (contents.none { it.type in SEED_TO_CROP }) return

        if (player.isOnCooldown(this)) {
            player.actionBar("<red>The Sowing Bundle is still recovering!")
            player.playSound(player.location, Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f)
            player.world.spawnParticle(Particle.SMOKE, player.location.add(0.0, 1.0, 0.0), 12, 0.3, 0.3, 0.3, 0.02)
            return
        }

        val seedsPlanted = sow(clickedBlock, contents)
        if (seedsPlanted == 0) return

        player.addCooldown(this, seedsPlanted.toLong() * 20L)
        player.playSound(player.location, Sound.ITEM_HOE_TILL, 1.0f, 1.2f)

        bundleMeta.setItems(contents)
        item.itemMeta = bundleMeta
    }

    private fun sow(origin: Block, contents: MutableList<ItemStack>): Int {
        val queue = ArrayDeque<Block>()
        val visited = mutableSetOf<Block>()
        var planted = 0

        queue.add(origin)
        visited.add(origin)

        while (queue.isNotEmpty()) {
            val block = queue.removeFirst()
            val above = block.getRelative(BlockFace.UP)

            if (above.type == Material.AIR) {
                val seedIndex = contents.indexOfFirst { it.type in SEED_TO_CROP }
                if (seedIndex == -1) return planted

                val seedStack = contents[seedIndex]
                above.type = SEED_TO_CROP[seedStack.type]!!

                val loc = above.location.add(0.5, 0.3, 0.5)
                above.world.spawnParticle(Particle.HAPPY_VILLAGER, loc, 4, 0.2, 0.15, 0.2, 0.0)

                seedStack.amount--
                if (seedStack.amount <= 0) contents.removeAt(seedIndex)
                planted++
            }

            for (face in ADJACENT_FACES) {
                val neighbor = block.getRelative(face)
                if (neighbor.type == Material.FARMLAND && neighbor !in visited) {
                    visited.add(neighbor)
                    queue.add(neighbor)
                }
            }
        }

        return planted
    }

    override fun onPrepareCraft(player: Player, event: PrepareItemCraftEvent) {
        val result = computeDyedBundleResult(event.inventory.matrix, KEY) ?: return
        event.inventory.result = result
    }

    override fun onCraftItem(player: Player, event: CraftItemEvent) {
        val result = computeDyedBundleResult(event.inventory.matrix, KEY) ?: return
        event.currentItem = result
    }
}
