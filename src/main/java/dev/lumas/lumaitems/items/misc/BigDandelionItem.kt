package dev.lumas.lumaitems.items.misc

import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.model.CustomItemFunctions
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.annotations.Disable
import dev.lumas.lumaitems.enums.WorldName
import dev.lumas.lumaitems.util.extensions.isMatchingItem
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

@Disable(WorldName.EVENT_NEW, WorldName.PINATA, hard = true)
class BigDandelionItem : CustomItemFunctions() {


    companion object {
        private val KEY = Util.namespacedKey("big-dandelion")
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
                AttributeContainer.of(KEY, Attribute.SCALE, AttributeModifier.Operation.ADD_NUMBER, 0.3, EquipmentSlotGroup.ANY),
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
}