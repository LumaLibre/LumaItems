package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CopyCoordinatesCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
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
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String> {
        return listOf("-yp")
    }

    override fun permission(): String {
        return "lumaitems.command.copycoordinates"
    }

    override fun playerOnly(): Boolean {
        return true
    }
}