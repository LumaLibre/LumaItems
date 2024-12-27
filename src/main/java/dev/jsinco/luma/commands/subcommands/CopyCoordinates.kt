package dev.jsinco.luma.commands.subcommands

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.commands.SubCommand
import dev.jsinco.luma.util.MiniMessageUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CopyCoordinates : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        sender as Player
        val coordinatesString = "${sender.world.name},${sender.location.blockX},${sender.location.blockY},${sender.location.blockZ}"
        MiniMessageUtil.msg(sender, Component.text(coordinatesString).clickEvent(ClickEvent.copyToClipboard(coordinatesString)).hoverEvent(Component.text("Click to copy")))
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }

    override fun permission(): String {
        return "lumaitems.command.copycoordinates"
    }

    override fun playerOnly(): Boolean {
        return true
    }
}