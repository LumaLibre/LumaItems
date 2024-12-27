package dev.jsinco.luma.commands.subcommands

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.commands.SubCommand
import dev.jsinco.luma.guis.AstralUpgradeGui
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class UpgradeCommand : SubCommand {

    companion object {
        val astralUpgradeGui = AstralUpgradeGui()
    }

    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        sender as Player
        sender.openInventory(astralUpgradeGui.getInventory())
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }

    override fun permission(): String {
        return "lumaitems.command.upgrade"
    }

    override fun playerOnly(): Boolean {
        return true
    }
}