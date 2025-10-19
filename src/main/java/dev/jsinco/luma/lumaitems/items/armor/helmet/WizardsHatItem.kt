package dev.jsinco.luma.lumaitems.items.armor.helmet

import dev.jsinco.luma.lumaitems.enums.Action
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.util.tiers.Tier
import dev.jsinco.luma.lumaitems.items.ItemFactory
import dev.jsinco.luma.lumaitems.manager.CustomItem
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.LeatherArmorMeta


class WizardsHatItem : CustomItem {

    val colors = listOf(
        Util.hex2BukkitColor("#40506a"),
        Util.hex2BukkitColor("#e0be6b"),
        Util.hex2BukkitColor("#b2b0ca"),
        Util.hex2BukkitColor("#93b3cf")
    )

    override fun createItem(): Pair<String, ItemStack> {
        val key = "wizardshat"
        val item = ItemFactory.builder()
            .name("<b><#41516B>W<#77766B>i<#AD9B6C>z<#E3C06C>a<#B3A679>r<#848B85>d<#547192>'<#8C91AE>s <#B4B2CC>H<#A5B4CF>a<#95B5D2>t</b>")
            .customEnchants("<gray>Unbreakable", "<#41516B>Revitalise")
            .lore("No lore yet")
            .material(Material.LEATHER_HELMET)
            .persistentData(key)
            .tier(Tier.CARNIVAL_2024)
            .unbreakable(true)
            .vanillaEnchants(mutableMapOf(Enchantment.MENDING to 1))
            .attributeModifiers(
                DefaultAttributes.NETHERITE_HELMET.appendThenGetAttributes(
                Attribute.MAX_HEALTH, key, 6.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.HEAD))
            .build().createItem()

        item.itemMeta = (item.itemMeta as? LeatherArmorMeta)?.apply {
            setColor(colors.random())
        }

        return Pair(key, item)
    }

    override fun executeActions(type: Action, player: Player, event: Any): Boolean {
        when (type) {
            Action.RUNNABLE -> {
                if (!Util.isItemInSlot("wizardshat", EquipmentSlot.HEAD, player)) {
                    return false
                }
                val item = player.equipment?.helmet
                item?.itemMeta = (item?.itemMeta as? LeatherArmorMeta)?.apply {
                    setColor(colors.random())
                }
            }

            else -> return false
        }
        return true
    }
}