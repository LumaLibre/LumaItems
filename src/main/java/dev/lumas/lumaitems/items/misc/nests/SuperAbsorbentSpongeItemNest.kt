package dev.lumas.lumaitems.items.misc.nests

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.Executors
import dev.lumas.lumaitems.util.Executors.syncEntityDelayed
import dev.lumas.lumaitems.util.extensions.setAirWithLog
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.ItemStack

abstract class SuperAbsorbentSpongeItemNest(val material: Material) : CustomItemFunctions() {

    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        event.isCancelled = true
        if (event.blockReplacedState.type != material) {
            return
        }

        player.syncEntityDelayed(1) {
            event.block.setAirWithLog(player)
        }
        removeNearbyBlocks(event.block, 3, material, player)
    }

    protected fun baseItem(mate: String): ItemFactory.Builder {
        return ItemFactory.builder()
            .material(Material.SPONGE)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .tier(Tier.SUMMER_2025)
            .lore(
                "This sponge is capable of",
                "soaking up any amount of",
                "$mate without needing to",
                "be dried out.",
                "",
                "Place in any body of $mate",
                "to start absorbing."
            )
    }


    private fun removeNearbyBlocks(sponge: Block, radius: Int, type: Material, player: Player) {

        val world = sponge.world
        val location = sponge.location

        val sphere = Sphere(location, radius.toDouble(), 0.0)
        val blocksInSphere = sphere.sphereFast

        for (targetBlock in blocksInSphere) {
            if (Material.WATER == type && targetBlock is Waterlogged && targetBlock.isWaterlogged) {
                targetBlock.breakNaturallyWithLog(player)
            }
            if (targetBlock.type != type) continue
            targetBlock.setAirWithLog(player)
            world.spawnParticle(Particle.CLOUD, targetBlock.location.add(0.5, 0.5, 0.5), 10, 0.2, 0.2, 0.2, 0.02)
        }

        world.playSound(location, Sound.BLOCK_FIRE_EXTINGUISH, 1.0f, 1.5f)
        world.playSound(location, Sound.BLOCK_WET_GRASS_BREAK, 1.0f, 1.0f)
    }

}

class SuperAbsorbentWaterSpongeItem : SuperAbsorbentSpongeItemNest(Material.WATER) {

    override fun createItem(): Pair<String, ItemStack> {
        return baseItem("<#0098de>water</#0098de>")
            .name("<b><gradient:#0098de:#718bf8:#145fdc>Absorbent Sponge</gradient></b>")
            .customEnchants("<#0098de>Super Absorbent")
            .persistentData("absorbent-water-sponge")
            .buildPair()
    }

}

class SuperAbsorbentLavaSpongeItem : SuperAbsorbentSpongeItemNest(Material.LAVA) {

    override fun createItem(): Pair<String, ItemStack> {
        return baseItem("<#FF4500>lava</#FF4500>")
            .name("<b><gradient:#f4521c:#e9910d:#e42800>Absorbent Sponge</gradient></b>")
            .customEnchants("<#FF4500>Super Absorbent")
            .persistentData("absorbent-lava-sponge")
            .buildPair()
    }

}