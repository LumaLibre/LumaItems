package dev.jsinco.luma.lumaitems.items.armor.boots

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.jsinco.luma.lumaitems.events.items.BlockCacheManager
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.shapes.ShapeUtil
import dev.jsinco.luma.lumaitems.util.Util
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import java.util.UUID
import kotlin.random.Random
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.data.Levelled
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class HellStridersItem : CustomItemFunctions() {


    companion object {
        private const val ID = "hellstriders"
    }

    override fun isDisabled(inLocation: Location): Boolean {
        return false
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.Companion.builder()
            .name("<b><#C93907>H<#D15112>e<#DA691D>l<#E28128>l<#E69635> <#E9AA43>S<#EDBF50>t<#E9AA43>r<#E69635>i<#E28128>d<#DA691D>e<#D15112>r<#C93907>s")
            .customEnchants("<#E06A41>Lava Walker I")
            .lore(
                "Allows the wearer to",
                "walk on lava."
            )
            .material(Material.NETHERITE_BOOTS)
            .persistentData(ID)
            .vanillaEnchants(
                Enchantment.MENDING to 1,
                Enchantment.UNBREAKING to 10,
                Enchantment.PROTECTION to 4,
                Enchantment.FIRE_PROTECTION to 9,
                Enchantment.SOUL_SPEED to 3
            )
            .tier(Tier.SUMMER_2025)
            .buildPair()
    }

    override fun onCachedBlockBreak(player: Player, event: BlockBreakEvent) {
        event.isCancelled = true
        event.block.type = Material.LAVA
        BlockCacheManager.unCacheBlock(player.uniqueId, event.block)
    }

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        if (!Util.isItemInSlot(ID, EquipmentSlot.FEET, player)) {
            delete(player.uniqueId)
        }
    }

    override fun onMove(player: Player, event: PlayerMoveEvent) {
        if (!event.hasChangedBlock() || player.isFlying || !player.location.add(0.0, -1.0, 0.0).block.isSolid) {
            return
        }

        val locBelow = player.location.add(0.0, -1.0, 0.0)
        if (!checkForAdjacentBlockType(locBelow.block, Material.LAVA)) {
            return
        }



        val blockBelow = locBelow.block


        for (block in ShapeUtil.circle(blockBelow.location, 4, 30)) {
            if ((block.type == Material.LAVA) && (block.blockData as Levelled).level == 0 && block.location.add(0.0, 1.0, 0.0).block.isEmpty) {
                block.type = Material.OBSIDIAN
                BlockCacheManager.cacheBlock(player.uniqueId, block, ID)
            }
        }
    }

    override fun onAsyncRunnable(player: Player) {
        val blocks: MutableList<Block> = BlockCacheManager.getCachedBlocks(ID).ifEmpty { return }.toMutableList()
        val locations: MutableSet<Location> = mutableSetOf()

        if (blocks.size >= 40) {
            for (index in 0 until blocks.size / 4) {
                Bukkit.getScheduler().runTask(instance(), Runnable {
                    val block = blocks[index]
                    if (block.type == Material.OBSIDIAN) {
                        block.type = Material.CRYING_OBSIDIAN
                    } else if (block.type == Material.CRYING_OBSIDIAN) {
                        block.type = Material.LAVA
                        BlockCacheManager.unCacheBlock(player.uniqueId, block)
                    }
                })
            }
        }

        for (i in 0 until 3) {
            Bukkit.getScheduler().runTaskLater(instance(), Runnable {
                for (e in 0 until 6) {
                    val block = blocks.random()
                    if (block.world != player.world || block.location.distance(player.location) > 10) {
                        block.type = Material.LAVA
                        BlockCacheManager.unCacheBlock(player.uniqueId, block)
                        continue
                    }

                    if (block.location in locations) continue
                    when (block.type) {
                        Material.OBSIDIAN -> {
                            block.type = Material.CRYING_OBSIDIAN
                            locations.add(block.location)
                        }

                        Material.CRYING_OBSIDIAN -> {
                            block.type = Material.LAVA
                            BlockCacheManager.unCacheBlock(player.uniqueId, block)
                            locations.add(block.location)
                        }
                        else -> continue
                    }
                }
            }, Random.Default.nextLong(1, 10))
        }
    }

    override fun onPlayerTeleport(player: Player, event: PlayerTeleportEvent) {
        delete(player.uniqueId)
    }

    override fun onPlayerQuit(player: Player, event: PlayerQuitEvent) {
        delete(player.uniqueId)
    }

    override fun onPluginDisable(player: Player) {
        delete(player.uniqueId)
    }


    private fun checkForAdjacentBlockType(center: Block, m: Material): Boolean {
        val loc = center.location
        return loc.clone().add(1.0, 0.0, 0.0).block.type == m || loc.clone().add(-1.0, 0.0, 0.0).block.type == m || loc.clone().add(0.0, 0.0, 1.0).block.type == m || loc.clone().add(0.0, 0.0, -1.0).block.type == m ||
                loc.clone().add(1.0, 0.0, 1.0).block.type == m || loc.clone().add(-1.0, 0.0, -1.0).block.type == m || loc.clone().add(-1.0, 0.0, 1.0).block.type == m || loc.clone().add(1.0, 0.0, -1.0).block.type == m
    }

    private fun delete(uuid: UUID) {
        val cachedBlocks: List<Block> = BlockCacheManager.getCachedBlocks(uuid).ifEmpty { return }
        BlockCacheManager.unCacheBlock(uuid, cachedBlocks)
        for (block in cachedBlocks) {
            block.type = Material.LAVA
        }
    }

}