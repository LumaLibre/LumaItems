package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.api.LumaItemsAPI
import dev.lumas.lumaitems.commands.CommandManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag


@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "hidetooltips",
    description = "Hide tool tips on a custom item",
    usage = "/<command> hidetooltips",
    permission = "lumaitems.command.hidetooltips",
    parent = CommandManager::class,
    playerOnly = true
)
class HideToolTipsCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack) {
        val player = src.sender as Player
        val item = player.inventory.itemInMainHand

        if (!LumaItemsAPI.getInstance().isCustomItem(item)) {
            Text.msg(player, "This item is not a custom item!")
            return
        }

        val defaultAttributes = item.type.defaultAttributeModifiers.entries().ifEmpty {
            Text.msg(player, "Couldn't find default attributes for this item! (Material: ${item.type})")
            return
        }

        val meta = item.itemMeta
        if (meta.hasAttributeModifiers()) {
            Text.msg(player, "Can't hide tool tips on this item. (Attribute modifiers already exist!)")
            return
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
    }

}