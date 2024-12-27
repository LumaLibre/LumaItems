package dev.jsinco.luma.lumaitems.commands.nonsub

import dev.jsinco.luma.lumaitems.commands.subcommands.UpgradeCommand
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UpgradeCMD : CommandExecutor {
    override fun onCommand(sender: CommandSender, p1: Command, p2: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: return false
        player.openInventory(UpgradeCommand.astralUpgradeGui.getInventory())
        return true
    }
}