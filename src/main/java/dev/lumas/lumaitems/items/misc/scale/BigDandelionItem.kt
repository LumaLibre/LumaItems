package dev.lumas.lumaitems.items.misc.scale

import com.destroystokyo.paper.event.player.PlayerArmorChangeEvent
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.SharedContainers
import dev.lumas.lumaitems.util.Tier
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.extensions.itemInMainHand
import dev.lumas.lumaitems.util.extensions.useNewSharedScaleContainer
import io.papermc.paper.event.entity.EntityCompostItemEvent
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

@Disable(WorldName.EVENT_NEW, WorldName.PINATA, hard = true)
class BigDandelionItem : CustomItemFunctions() {


    companion object {
        private val KEY = Util.namespacedKey("big-dandelion")
        private const val AMT = 0.3
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#bd6b23:#f19e26:#fed738:#fdec50:#197c05>Big Dandelion</gradient></b>")
            .customEnchants("<#fed738>Flower Powder")
            .material(Material.DANDELION)
            .persistentData(KEY)
            .tier(Tier.HALLOWEEN_2025)
            .vanillaEnchants(Enchantment.UNBREAKING to 10)
            .lore(
                "A dandelion so big,",
                "it looks like it could",
                "blow away an entire",
                "field of flowers.",
                "",
                "I wonder if holding it",
                "does anything special?"
            )
            .attributeModifiers(
                SharedContainers.SCALE.setOperation(AttributeModifier.Operation.ADD_NUMBER).setAmount(AMT).build(),
                AttributeContainer.of(KEY, Attribute.JUMP_STRENGTH , AttributeModifier.Operation.ADD_NUMBER, 0.1, EquipmentSlotGroup.ANY)
            )
            .autoHat(true)
            .buildPair()
    }


    override fun onPlaceBlock(player: Player, event: BlockPlaceEvent) {
        val item = event.itemInHand
        if (!item.isMatchingItem(KEY)) {
            return
        }
        event.isCancelled = true
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