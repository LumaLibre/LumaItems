package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.util.Text
import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "copycoordinates",
    description = "Copy your current coordinates to clipboard",
    usage = "/<command> copycoordinates",
    permission =  "lumaitems.command.copycoordinates",
    parent = CommandManager::class,
    playerOnly = true
)
class CopyCoordinatesCommand : SubCommand {

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        val coordinatesString = if (args.contains("-yp")) {
            "${sender.world.name},${sender.location.blockX},${sender.location.blockY},${sender.location.blockZ},${sender.location.yaw},${sender.location.pitch}"
        } else {
            "${sender.world.name},${sender.location.blockX},${sender.location.blockY},${sender.location.blockZ}"
        }

        val c = Util.getRandomColor()
        val comp = Component.text(coordinatesString)
            .clickEvent(ClickEvent.copyToClipboard(coordinatesString))
            .hoverEvent(Component.text("Click to copy"))
            .color(TextColor.color(c.red, c.green, c.blue))
        Text.msg(sender, comp)
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String> {
        return listOf("-yp")
    }

}