package dev.jsinco.luma.lumaitems.items.tools

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItemFunctions
import dev.jsinco.luma.lumaitems.obj.AttributeContainer
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class HeartStringHatchetItem : CustomItemFunctions() {
    override fun createItem(): Pair<String, ItemStack> {
        val key = "heartstring-hatchet"
        val extra = 5.5 // Little over 2x, but whatever
        return ItemFactory.builder()
            .name("<b><#FF8198>H<#FC8698>e<#F98C98>a<#F69199>r<#F29699>t<#EF9C99>s<#ECA199>t<#EBA4A9>r<#EAA6B9>i<#EAA9CA>n<#E9ABDA>g <#E9A8ED>H<#EBA1F0>a<#EC9BF3>t<#ED94F6>c<#EE8EF9>h<#F087FC>e<#F181FF>t")
            .customEnchants("<#EF9C99>Extend I")
            .material(Material.NETHERITE_AXE)
            .persistentData(key)
            .tier(Tier.VALENTIDE_2025)
            .lore(
                "Reach even further",
                "with this hatchet!",
                "",
                "<#FF8198>2x<white> your reach while",
                "holding this hatchet in",
                "your main hand."
            )
            .attributeModifiers(
                DefaultAttributes.NETHERITE_AXE.appendThenGetAttributes(
                    AttributeContainer.of(key, Attribute.BLOCK_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, extra, EquipmentSlotGroup.MAINHAND),
                    AttributeContainer.of(key, Attribute.ENTITY_INTERACTION_RANGE, AttributeModifier.Operation.ADD_NUMBER, extra, EquipmentSlotGroup.MAINHAND)
                )
            )
            .vanillaEnchants(
                Enchantment.SMITE to 6,
                Enchantment.BANE_OF_ARTHROPODS to 6,
                Enchantment.FORTUNE to 4,
                Enchantment.EFFICIENCY to 8,
                Enchantment.UNBREAKING to 14,
                Enchantment.MENDING to 1
            )
            .spoofEnchants(true)
            .buildPair()
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        return false // No abilities
    }

//    override fun onBreakBlock(player: Player, event: BlockBreakEvent) {
//        if (random().nextInt(400) > 2) {
//            return
//        }
//        val loc = event.block.location
//
//        player.world.spawnParticle(Particle.EXPLOSION, loc, 1, 0.3, 0.3, 0.3, 0.0)
//        player.world.playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 1.0f, 1.0f)
//        Sphere(loc, 4.0, 9.0)
//            .sphere
//            .filter { it.type == event.block.type }
//            .forEach { it.breakNaturally() }
//    }
}