package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.annotations.Ignore
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Player
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
