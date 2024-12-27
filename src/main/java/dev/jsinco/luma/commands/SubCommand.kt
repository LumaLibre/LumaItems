package dev.jsinco.luma.commands

import dev.jsinco.luma.LumaItems
import org.bukkit.command.CommandSender

interface SubCommand {

    fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>)

    fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>?

    fun permission(): String?

    fun playerOnly(): Boolean
}