package dev.jsinco.luma.lumaitems.items.tools.mattock

import dev.jsinco.luma.lumaitems.enums.BlockConstants
import dev.jsinco.luma.lumaitems.enums.CardinalDirection
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.shapes.Ellipsoid
import dev.jsinco.luma.lumaitems.util.AbilityUtil
import dev.jsinco.luma.lumaitems.util.extensions.BlockUtil.breakNaturallyWithLog
import dev.jsinco.luma.lumaitems.util.disabling.Disable
import dev.jsinco.luma.lumaitems.util.disabling.WorldName
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

@Disable(value = [WorldName.PINATA], hard = true)
class BigSwingPickaxeItem : CustomItemFunctions() {

    override fun createItem(): Pair<String, ItemStack> {
        val key = "big-swing-pickaxe"
        return ItemFactory.builder()
            .name("<b><gradient:#fc8585:#ffc86d:#fbdba2:#86cbd9:#68C3BB>Big Swing Pickaxe</gradient></b>")
            .customEnchants("<#86cbd9>Wide Swing", "<#68C3BB>Reach")
            .material(Material.NETHERITE_PICKAXE)
            .tier(Tier.SUMMER_2025)
            .persistentData(key)
            .lore(
                "With the large motor on this",
                "pickaxe, you'll be able to easily",
                "cut through any type of block!",
                "",
                "This tool breaks blocks in a",
                "wide area, at the cost of break",
                "speed.",
                "",
                "While holding this pickaxe, you",
                "may reach up to <#86cbd9>two</#86cbd9> blocks",
                "further than normal."
            )
            .vanillaEnchants(
                Enchantment.UNBREAKING to 4,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .attributeModifiers(
                DefaultAttributes.NETHERITE_PICKAXE.appendThenGetAttributes(
                    AttributeContainer.of(key, Attribute.BLOCK_BREAK_SPEED, AttributeModifier.Operation.ADD_NUMBER,-0.75, EquipmentSlotGroup.ANY),
                    AttributeContainer.of(key, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, -3.4, EquipmentSlotGroup.ANY),
                    AttributeContainer.of(key, Attribute.BLOCK_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, 2.0, EquipmentSlotGroup.MAINHAND),
                    AttributeContainer.of(key, Attribute.ENTITY_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, 2.0, EquipmentSlotGroup.MAINHAND)
                )
            )
            .buildPair()
    }


    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val item = player.inventory.itemInMainHand
        val cardinalDirection = CardinalDirection.fromEntityYaw(player)

        val block = event.block
        if (block.getBreakSpeed(player) >= Float.POSITIVE_INFINITY) {
            return
        }


        val xRadius = if (cardinalDirection == CardinalDirection.EAST || cardinalDirection == CardinalDirection.WEST) 2.5 else 4.0
        val zRadius = if (cardinalDirection == CardinalDirection.NORTH || cardinalDirection == CardinalDirection.SOUTH) 2.5 else 4.0


        val ellipsoidBlocks = Ellipsoid.getEllipsoid(block.location, xRadius, 2.0, zRadius).filter {
            !BlockConstants.BLACKLISTED.contains(it.type) && it.isSolid && it != block
        }
        for (block in ellipsoidBlocks) {
            block.breakNaturallyWithLog(player, item, true)
        }
    }
}