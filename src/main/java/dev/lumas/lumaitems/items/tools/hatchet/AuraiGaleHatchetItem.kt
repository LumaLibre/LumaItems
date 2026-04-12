package dev.lumas.lumaitems.items.tools.hatchet

import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.model.item.AttributeContainer
import dev.lumas.lumaitems.model.item.CustomItemFunctions
import dev.lumas.lumaitems.util.Kind
import dev.lumas.lumaitems.util.extensions.isLocationOnGround
import dev.lumas.lumaitems.util.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockDamageEvent
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class AuraiGaleHatchetItem : CustomItemFunctions() {

    private companion object {
        private const val KEY = "aurai-gale-hatchet"
    }

    override fun createItem(): Pair<String, ItemStack> {
        return ItemFactory.builder()
            .name("<b><gradient:#F8BFE7:#F8C2B4:#FAEBB3:#B8E2FC:#9EB4EC>Aurai Gale Hatchet</gradient></b>")
            .customEnchants("<#F8BFE7>Frisk")
            .material(Material.NETHERITE_AXE)
            .persistentData(KEY)
            .tier(Tier.WONDERLAND_2026)
            .lore(
                "<#F8BFE7>Holding</#F8BFE7> this axe will",
                "significantly increase",
                "your movement speed.",
                "",
                "While <#F8BFE7>falling</#F8BFE7> or while",
                "in <#F8BFE7>water</#F8BFE7>, this axe will",
                "not lose any efficiency",
                "but not both at once.",
            )
            .vanillaEnchants(
                Enchantment.EFFICIENCY to 10,
                Enchantment.SILK_TOUCH to 1,
                Enchantment.UNBREAKING to 4,
                Enchantment.MENDING to 1
            )
            .attributeModifiers(
                AttributeContainer.builder()
                    .setKey(KEY)
                    .setAttribute(Attribute.MOVEMENT_SPEED)
                    .setOperation(AttributeModifier.Operation.ADD_NUMBER)
                    .setAmount(0.025)
                    .setSlot(EquipmentSlotGroup.MAINHAND)
                    .build()
            )
            .buildPair()
    }

    override fun onBlockDamage(player: Player, event: BlockDamageEvent) {
        val item = player.inventory.itemInMainHand
        val type = event.block.type
        if (Kind.WOODS.isTagged(type) && player.canInstaBreak()) {
            event.instaBreak = true
            item.damage(1, player)
        }
    }

    private fun Player.canInstaBreak(): Boolean {
        return isLocationOnGround().not() xor isInWater
    }
}