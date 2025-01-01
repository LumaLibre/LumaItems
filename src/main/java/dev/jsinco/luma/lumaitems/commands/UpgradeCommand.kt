package dev.jsinco.luma.lumaitems.commands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.commands.subcommands.UpgradeCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.COMMAND)
@CommandInfo(
    name = "upgrade",
    description = "Open the Astral Upgrade GUI",
    usage = "/<command>",
    playerOnly = true
)
class UpgradeCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: return false
        player.openInventory(UpgradeCommand.astralUpgradeGui.getInventory())
        return true
    }
}