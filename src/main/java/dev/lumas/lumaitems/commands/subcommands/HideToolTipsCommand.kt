package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.api.LumaItemsAPI
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.enums.DefaultAttributes
import dev.lumas.lumaitems.util.MiniMessageUtil
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag


@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "hidetooltips",
    description = "Hide tool tips on a custom item",
    usage = "/<command> hidetooltips",
    permission = "lumaitems.command.hidetooltips",
    parent = CommandManager::class,
    playerOnly = true
)
class HideToolTipsCommand : SubCommand {

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val player = sender as Player
        val item = player.inventory.itemInMainHand

        if (!LumaItemsAPI.getInstance().isCustomItem(item)) {
            MiniMessageUtil.msg(player, "This item is not a custom item!")
            return true
        }

        val defaultAttributes = DefaultAttributes.getFromMaterial(item.type) ?: run {
            MiniMessageUtil.msg(player, "Couldn't find default attributes for this item! (Material: ${item.type})")
            return true
        }

        val meta = item.itemMeta
        if (meta.hasAttributeModifiers()) {
            MiniMessageUtil.msg(player, "Can't hide tool tips on this item. (Attribute modifiers already exist!)")
            return true
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
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }

}