package dev.lumas.lumaitems.items.misc.scale

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItemFunctions
import io.papermc.paper.event.entity.EntityCompostItemEvent
import dev.lumas.lumaitems.util.extensions.isInAnySlot
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.isWearing
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.tiers.Tier
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
        private val MINI_POPPY = "mini-poppy".namespacedKey() // TODO: replace this with global attributes
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
                AttributeContainer.of(MINI_POPPY, Attribute.SCALE, AttributeModifier.Operation.ADD_NUMBER, -0.7, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.GRAVITY , AttributeModifier.Operation.ADD_NUMBER, -0.04, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.SAFE_FALL_DISTANCE, AttributeModifier.Operation.ADD_NUMBER, 6.0, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.JUMP_STRENGTH , AttributeModifier.Operation.ADD_NUMBER, 0.06, EquipmentSlotGroup.ANY),
                AttributeContainer.of(KEY, Attribute.MAX_HEALTH , AttributeModifier.Operation.ADD_NUMBER, -4.0, EquipmentSlotGroup.ANY)
            )
            .autoHat(true)
            .buildPair()
    }


    override fun onArmorChange(player: Player, event: PlayerArmorChangeEvent) {
        event.newItem.doModify()
    }

    override fun onPlayerSwapHands(player: Player, event: PlayerSwapHandItemsEvent) {
        event.player.inventory.itemInMainHand.doModify()
    }

    override fun onPlayerItemHeld(player: Player, event: PlayerItemHeldEvent) {
        player.inventory.itemInMainHand.doModify()
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

    private fun ItemStack.doModify() {
        if (true) return // TODO
        val item = this.takeIf { it.isMatchingItem(KEY) } ?: return
        item.editMeta { meta ->
            if (meta.getAttributeModifiers(Attribute.SCALE)?.any { it.key == MINI_POPPY } == true) return@editMeta

            meta.removeAttributeModifier(Attribute.SCALE)
            meta.addAttributeModifier(Attribute.SCALE, AttributeModifier(MINI_POPPY, -0.7, AttributeModifier.Operation.ADD_NUMBER))
        }
    }
}