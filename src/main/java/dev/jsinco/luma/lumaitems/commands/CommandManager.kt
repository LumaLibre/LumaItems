package dev.jsinco.luma.lumaitems.commands

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.subcommands.AddTier
import dev.jsinco.luma.lumaitems.commands.subcommands.CopyCoordinates
import dev.jsinco.luma.lumaitems.commands.subcommands.DebugCommand
import dev.jsinco.luma.lumaitems.commands.subcommands.GiveAstralCommand
import dev.jsinco.luma.lumaitems.commands.subcommands.GiveItemCommand
import dev.jsinco.luma.lumaitems.commands.subcommands.PinataFileCommand
import dev.jsinco.luma.lumaitems.commands.subcommands.RelicCommand
import dev.jsinco.luma.lumaitems.commands.subcommands.UpgradeCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.entity.Player

class CommandManager(val plugin: LumaItems) : CommandExecutor, TabCompleter {

    val commands: MutableMap<String, SubCommand> = mutableMapOf()

    init {
        commands["give"] = GiveItemCommand()
        commands["pinatafile"] = PinataFileCommand()
        commands["debug"] = DebugCommand()
        commands["relic"] = RelicCommand()
        commands["giveastral"] = GiveAstralCommand()
        commands["addtier"] = AddTier()
        commands["upgrade"] = UpgradeCommand()
        commands["copycoordinates"] = CopyCoordinates()
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false

        val subCommand = commands[args[0]] ?: return false

        if (subCommand.playerOnly() && sender !is Player) return false
        else if (subCommand.permission() != null && !sender.hasPermission(subCommand.permission()!!)) return false

        subCommand.execute(plugin, sender, args)
        return true
    }

    override fun onTabComplete(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String>? {
        if (args.size == 1) return commands.keys.toList()

        val subCommand = commands[args[0]] ?: return null

        if (subCommand.playerOnly() && sender !is Player) return null
        else if (subCommand.permission() != null && !sender.hasPermission(subCommand.permission()!!)) return null

        return subCommand.tabComplete(plugin, sender, args)
    }

}