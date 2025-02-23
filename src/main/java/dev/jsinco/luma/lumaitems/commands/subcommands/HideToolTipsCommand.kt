package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.api.LumaItemsAPI
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.enums.DefaultAttributes
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag

class HideToolTipsCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        val player = sender as Player
        val item = player.inventory.itemInMainHand

        if (!LumaItemsAPI.getInstance().isCustomItem(item)) {
            MiniMessageUtil.msg(player, "This item is not a custom item!")
            return
        }

        val defaultAttributes = DefaultAttributes.getFromMaterial(item.type) ?: run {
            MiniMessageUtil.msg(player, "Couldn't find default attributes for this item! (Material: ${item.type})")
            return
        }

        val meta = item.itemMeta
        if (meta.hasAttributeModifiers()) {
            MiniMessageUtil.msg(player, "Can't hide tool tips on this item. (Attribute modifiers already exist!)")
            return
        }
        meta.addItemFlags(
            ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ARMOR_TRIM,
            ItemFlag.HIDE_UNBREAKABLE,
            ItemFlag.HIDE_DYE,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP
        )
        for (attributeModifier in defaultAttributes.attributes) {
            meta.addAttributeModifier(attributeModifier.key, attributeModifier.value)
        }


        item.itemMeta = meta
        MiniMessageUtil.msg(player, "Tool tips have been hidden!")
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }

    override fun permission(): String? {
        return "lumaitems.command.hidetooltips"
    }

    override fun playerOnly(): Boolean {
        return true
    }
}