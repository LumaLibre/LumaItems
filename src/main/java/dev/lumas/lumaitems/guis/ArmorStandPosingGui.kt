package dev.lumas.lumaitems.guis

import io.papermc.paper.math.Rotations
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory

class ArmorStandPosingGui(val armorStand: ArmorStand) : AbstractGui {

    companion object {
        val ROTATIONS_LORE = mutableListOf(
            "<gray>Left click to adjust X",
            "<gray>Middle click to adjust Y",
            "<gray>Right click to adjust Z",
            "<gray>Shift + click to reset",
            "<yellow><b>Current Rotations</b><gray>: <white>%s"
        )

        fun rotationsAsString(rotations: Rotations): String {
            return String.format("%.1f, %.1f, %.1f", rotations.x(), rotations.y(), rotations.z())
        }

        fun rotationsLore(rotations: Rotations): List<String> {
            return ROTATIONS_LORE.map { it.replace("%s", rotationsAsString(rotations)) }
        }
    }

    private val TOGGLE_BASEPLATE = GuiItemBuilder.create(Material.SMOOTH_STONE_SLAB)
        .displayName("<yellow><b>Toggle Baseplate")
        .lore(listOf("<gray>Click to toggle the baseplate"))
        .data(listOf("baseplate"))
        .glint(armorStand.hasBasePlate())
        .build()

    private val HEAD_ROTATIONS = GuiItemBuilder.create(Material.PLAYER_HEAD)
        .displayName("<yellow><b>Head Rotations")
        .lore(rotationsLore(armorStand.headRotations))
        .data(listOf("head-rotations"))
        .build()

    private val BODY_ROTATIONS = GuiItemBuilder.create(Material.LEATHER_CHESTPLATE)
        .displayName("<yellow><b>Body Rotations")
        .lore(rotationsLore(armorStand.bodyRotations))
        .data(listOf("body-rotations"))
        .build()

    private val LEFT_ARM_ROTATIONS = GuiItemBuilder.create(Material.LEATHER_BOOTS)
        .displayName("<yellow><b>Left Arm Rotations")
        .lore(rotationsLore(armorStand.leftArmRotations))
        .data(listOf("left-arm-rotations"))
        .build()

    override fun onInventoryClick(event: InventoryClickEvent) {

        TODO("Not yet implemented")
    }

    override fun onInventoryClose(event: InventoryCloseEvent) {
        TODO("Not yet implemented")
    }

    override fun getInventory(): Inventory {
        TODO("Not yet implemented")
    }
}