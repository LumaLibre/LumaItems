package dev.jsinco.luma.lumaitems.commands

import dev.jsinco.luma.lumacore.manager.commands.AbstractCommand
import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.guis.AstralUpgradeGui
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.COMMAND)
@CommandInfo(
    name = "upgrade",
    description = "Open the Astral upgrade gui",
    usage = "/<command>",
    playerOnly = true
)
class UpgradeCommand : AbstractCommand() {

    companion object {
        private val astralUpgradeGui = AstralUpgradeGui()
    }

    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        sender.openInventory(astralUpgradeGui.getInventory())
        return true
    }

    override fun handleTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String>? {
        return null
    }
}