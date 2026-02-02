package dev.lumas.lumaitems.items.tools.spade

import dev.lumas.lumaitems.enums.BlockConstants
import dev.lumas.lumaitems.enums.CardinalDirection
import dev.lumas.lumaitems.enums.DefaultAttributes
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.manager.CustomItemFunctions
import dev.lumas.lumaitems.model.AttributeContainer
import dev.lumas.lumaitems.shapes.Ellipsoid
import dev.lumas.lumaitems.util.extensions.breakNaturallyWithLog
import dev.lumas.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.Tag
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType

// TODO: create item nest
class BigSwingSpadeItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        val key = "big-swing-spade"
        return ItemFactory.builder()
            .name("<b><gradient:#c82f3b:#ed7b85:#68a5a3:#346e56>Big Swing Spade</gradient></b>")
            .customEnchants("<#ed7b85>Wide Swing")
            .material(Material.NETHERITE_SHOVEL)
            .tier(Tier.CHRISTMAS_2025)
            .persistentData(key)
            .lore(
                "A larger-than-normal spade,",
                "fitted with a large scoop to",
                "make digging easier.",
                "",
                "This tool <#ed7b85>breaks</#ed7b85> blocks",
                "in a wide area, at the cost",
                "of break speed.",
            )
            .vanillaEnchants(
                Enchantment.UNBREAKING to 4,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.MENDING to 1
            )
            .attributeModifiers(
                DefaultAttributes.NETHERITE_SHOVEL.appendThenGetAttributes(
                    AttributeContainer.of(key, Attribute.BLOCK_BREAK_SPEED, AttributeModifier.Operation.ADD_NUMBER,-0.86, EquipmentSlotGroup.ANY),
                    AttributeContainer.of(key, Attribute.ATTACK_SPEED, AttributeModifier.Operation.ADD_NUMBER, -3.4, EquipmentSlotGroup.ANY)
                )
            )
            .buildPair()
    }

    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
        val item = player.inventory.itemInMainHand
        val block = event.block
        if (!Tag.MINEABLE_SHOVEL.isTagged(block.type) || block.getBreakSpeed(player) >= Float.POSITIVE_INFINITY) {
            return
        }

        val cardinalDirection = CardinalDirection.fromEntityYaw(player)


        val xRadius = if (cardinalDirection == CardinalDirection.EAST || cardinalDirection == CardinalDirection.WEST) 2.5 else 4.0
        val zRadius = if (cardinalDirection == CardinalDirection.NORTH || cardinalDirection == CardinalDirection.SOUTH) 2.5 else 4.0


        val ellipsoidBlocks = Ellipsoid.getEllipsoid(block.location, xRadius, 2.0, zRadius).filter {
            !BlockConstants.BLACKLISTED.contains(it.type) && it.isSolid && it != block && Tag.MINEABLE_SHOVEL.isTagged(it.type)
        }
        for (block in ellipsoidBlocks) {
            block.breakNaturallyWithLog(player, item, true)
        }

        if (player.hasPotionEffect(PotionEffectType.HASTE)) {
            player.removePotionEffect(PotionEffectType.HASTE)
        }
    }
}