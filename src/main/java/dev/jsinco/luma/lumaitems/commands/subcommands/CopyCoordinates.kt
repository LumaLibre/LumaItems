package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CopyCoordinates : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        sender as Player
        val coordinatesString = if (args.contains("-yp")) {
            "${sender.world.name},${sender.location.blockX},${sender.location.blockY},${sender.location.blockZ},${sender.location.yaw},${sender.location.pitch}"
        } else {
            "${sender.world.name},${sender.location.blockX},${sender.location.blockY},${sender.location.blockZ}"
        }
        MiniMessageUtil.msg(sender, Component.text(coordinatesString).clickEvent(ClickEvent.copyToClipboard(coordinatesString)).hoverEvent(Component.text("Click to copy")))
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