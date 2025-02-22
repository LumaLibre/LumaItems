package dev.jsinco.luma.lumaitems.items.playground

import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.util.disabling.Ignore
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

@Ignore
class PosableArmorStandItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<reset><white>Armor Stand (Posable)")
            .material(Material.ARMOR_STAND)
            .persistentData("posablearmorstand")
            .buildPair()
    }


    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val block = event.clickedBlock ?: return
        val face = event.blockFace

        if (face == BlockFace.DOWN) return

        // spawn the armor stand 1 block away from the face
        val location = block.location.clone().add(face.direction)
        val armorStand = location.world?.spawn(location, ArmorStand::class.java) ?: return

        armorStand.setArms(true)
    }




    enum class Pose {

    }

}
