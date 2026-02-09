package dev.lumas.lumaitems.items.tools

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.QuickTasks
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

//burrow something
class UnnamedShovelItem : CustomItemFunctions() {

    companion object {
        private val TRANSFORMATIONS = mapOf(
            Material.GRASS_BLOCK to Material.DIRT,
            Material.DIRT to Material.GRASS_BLOCK,
            Material.SAND to Material.RED_SAND,
            Material.RED_SAND to Material.SAND,
        )
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Unnamed Shovel")
            .persistentData("unnamed-shovel")
            .material(Material.NETHERITE_SHOVEL)
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {
        if (!player.isSneaking || QuickTasks.isOnCooldown(this, player)) return
        QuickTasks.addCooldown(this, player, 20)

        val clickedBlock = event.clickedBlock ?: return

        for (transformation in TRANSFORMATIONS) {
            if (clickedBlock.type == transformation.key) {
                transformCircle(clickedBlock, transformation.key, transformation.value)
                player.world.playSound(clickedBlock.location, Sound.BLOCK_SPONGE_ABSORB, 1f, 1f)
                event.isCancelled = true
                break
            }
        }
    }

    private fun transformCircle(clickedBlock: Block, type: Material, transformTo: Material) {
        val sphere = Sphere(clickedBlock.location, 3.0, 20.0)
        sphere.sphere.forEach {block ->
            if (type == block.type) {
                block.type = transformTo
            }
        }
    }
}