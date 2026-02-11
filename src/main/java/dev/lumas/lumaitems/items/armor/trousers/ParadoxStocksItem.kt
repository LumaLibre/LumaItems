package dev.lumas.lumaitems.items.armor.trousers

import dev.lumas.lumaitems.enums.SimpleDirection
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

@Disable(WorldName.SPAWN, WorldName.PINATA, WorldName.EVENT, WorldName.EVENT_NEW, WorldName.EVENT_THE_END)
class ParadoxStocksItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Paradox Stocks")
            .persistentData("paradox-stocks")
            .material(Material.NETHERITE_LEGGINGS)
            .buildPair()
    }


    fun SimpleDirection.canFitHumanEntity(hitBlock: Block): Boolean {
        // Check if the block in the direction of the hit block is air, and the block below that is also air
        val first = hitBlock.getRelative(this.blockFace)
        val second = first.getRelative(BlockFace.DOWN)
        return first.isEmpty && second.isEmpty
    }

    fun SimpleDirection.teleportLocation(hitBlock: Block): Location {
        val first = hitBlock.getRelative(this.blockFace)
        return first.location.add(0.5, -1.0, 0.5)
    }

    fun tryTeleport(player: Player, clickedBlock: Block) {
        // if direction is up/down we spawn a raytrace on the OTHER side of the blok and check if it can fit there, otherwise we pass in the clicked block
        val direction = SimpleDirection.fromLocation(player.eyeLocation)
        println("testing direction $direction")
        val otherSide = clickedBlock.getRelative(direction.blockFace)
        if (direction.canFitHumanEntity(otherSide)) {
            // teleport player to hit block
            val teleportLocation = direction.teleportLocation(otherSide)
            teleportLocation.yaw = player.eyeLocation.yaw
            teleportLocation.pitch = player.eyeLocation.pitch
            player.teleportAsync(teleportLocation)
        }
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val clickedBlock = event.clickedBlock ?: return
        tryTeleport(player, clickedBlock)
    }
}