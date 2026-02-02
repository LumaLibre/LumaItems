package dev.lumas.lumaitems.items.weapons.spear

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.model.PaperDataComponent
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.SwingAnimation
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack


class UnnamedSpearItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("Testing Sword")
            .material(Material.NETHERITE_PICKAXE)
            .persistentData("testing-sword")
            .paperDataComponents(PaperDataComponent.valued(DataComponentTypes.SWING_ANIMATION, SwingAnimation.swingAnimation().type(
                SwingAnimation.Animation.STAB).duration(20).build()))
            .buildPair()
    }

    override fun onRightClick(player: Player, event: PlayerInteractEvent) {

    }
}