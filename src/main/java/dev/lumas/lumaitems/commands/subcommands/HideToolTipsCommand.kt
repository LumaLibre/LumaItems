package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.api.LumaItemsAPI
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag


@Register(Autowire.SUBCOMMAND)
@CommandMeta(
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
            Text.msg(player, "This item is not a custom item!")
            return true
        }

        val defaultAttributes = item.type.defaultAttributeModifiers.entries().ifEmpty {
            Text.msg(player, "Couldn't find default attributes for this item! (Material: ${item.type})")
            return true
        }

        val meta = item.itemMeta
        if (meta.hasAttributeModifiers()) {
            Text.msg(player, "Can't hide tool tips on this item. (Attribute modifiers already exist!)")
            return true
        }
        meta.addItemFlags(
            ItemFlag.HIDE_ATTRIBUTES,
            ItemFlag.HIDE_ARMOR_TRIM,
            ItemFlag.HIDE_UNBREAKABLE,
            ItemFlag.HIDE_DYE,
            ItemFlag.HIDE_ADDITIONAL_TOOLTIP
        )
        for (attributeModifier in defaultAttributes) {
            meta.addAttributeModifier(attributeModifier.key, attributeModifier.value)
        }


        item.itemMeta = meta
        Text.msg(player, "Tool tips have been hidden!")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }

}