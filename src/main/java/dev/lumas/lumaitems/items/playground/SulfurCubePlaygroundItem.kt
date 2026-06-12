package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import org.bukkit.Material
import org.bukkit.craftbukkit.entity.CraftSulfurCube
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player
import org.bukkit.entity.SulfurCube
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class SulfurCubePlaygroundItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("sulfur cubes")
            .persistentData("sulfur-cubes")
            .material(Material.DIAMOND_PICKAXE)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        val itemStack = ItemStack.of(Material.OAK_LOG, 1)

        val nmsItemStack = (itemStack as CraftItemStack).handle

        val sulfurCube = player.world.spawn(player.location, SulfurCube::class.java)

        sulfurCube.equipment.setItem(EquipmentSlot.BODY, itemStack)

        val craftSulfurCube = sulfurCube as CraftSulfurCube
        val nmsSulfurCube = craftSulfurCube.handle

        //nmsSulfurCube.matchingArchetypes()
        //println(nmsSulfurCube.matchingArchetypes(nmsItemStack))
    }
}