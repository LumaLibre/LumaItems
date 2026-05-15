package dev.lumas.lumaitems.items.misc.scale

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.SharedContainers
import io.papermc.paper.event.entity.EntityCompostItemEvent
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.extensions.itemInMainHand
import dev.lumas.lumaitems.util.extensions.useNewSharedScaleContainer
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

@Disable(value = [WorldName.EVENT_NEW, WorldName.PINATA], hard = true)
class MiniMushroomItem : CustomItemFunctions() {

    private companion object {
        private const val KEY = "mini-mushroom"
        private const val AMT = -0.7
    }

    override fun createItem(): Pair<String, ItemStack> {

        return ItemFactory.builder()
            .name("<b><gradient:#c75555:#eea3a2:#d084a6:#D38E92:#c78de1>Mini Mushroom</gradient></b>")
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .customEnchants("<#d084a6>Right Side")
            .lore(
                "This little toadstool",
                "makes anything that it",
                "touches smaller!",
                "",
                "<#d084a6>Hold</#d084a6> this mushroom to",
                "reduce your size at",
                "the cost of health."
            )
            .tier(Tier.VALENTIDE_2026)
            .persistentData(KEY)
            .material(Material.RED_MUSHROOM)
            .attributeModifiers(
                SharedContainers.SCALE.setOperation(AttributeModifier.Operation.ADD_NUMBER).setAmount(AMT).build(),
                AttributeContainer.of(KEY, Attribute.GRAVITY , AttributeModifier.Operation.ADD_NUMBER, -0.04, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.SAFE_FALL_DISTANCE, AttributeModifier.Operation.ADD_NUMBER, 6.0, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.JUMP_STRENGTH , AttributeModifier.Operation.ADD_NUMBER, 0.06, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.MAX_HEALTH , AttributeModifier.Operation.ADD_NUMBER, -4.0, EquipmentSlotGroup.ANY)
            )
            .autoHat(true)
            .buildPair()
    }


    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (item.isMatchingItem(KEY)) {
            event.isCancelled = true
        }
    }

    override fun onEntityCompostItem(event: EntityCompostItemEvent) {
        if (!event.item.isMatchingItem(KEY)) return
        event.isCancelled = true
    }

    // TODO: Temporary VV

    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        event.newItem.useNewSharedScaleContainer(KEY, AMT)
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        event.player.itemInMainHand.useNewSharedScaleContainer(KEY, AMT)
    }

    override fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {
        player.itemInMainHand.useNewSharedScaleContainer(KEY, AMT)
    }
}