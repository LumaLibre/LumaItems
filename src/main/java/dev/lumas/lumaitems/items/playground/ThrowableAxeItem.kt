package dev.lumas.lumaitems.items.playground

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.model.PaperDataComponent
import dev.lumas.lumaitems.util.disabling.Ignore
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.event.entity.ProjectileLaunchEvent
import org.bukkit.inventory.ItemStack

@Ignore
class ThrowableAxeItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Throwable Axe")
            .material(org.bukkit.Material.TRIDENT)
            .persistentData("throwable-axe")
            .paperDataComponents(
                PaperDataComponent.valued(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft("diamond_axe"))
            )
            .buildPair()
    }

    override fun onProjectileLaunch(player: Player, event: ProjectileLaunchEvent) {
        val trident = event.entity as? org.bukkit.entity.Trident ?: return
        trident.remove()
        val arrow = player.launchProjectile(org.bukkit.entity.Arrow::class.java)
        arrow.itemStack = ItemStack(org.bukkit.Material.DIAMOND_AXE)
    }
}