package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
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
        MiniMessageUtil.msg(sender, comp)
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String> {
        return listOf("-yp")
    }

}