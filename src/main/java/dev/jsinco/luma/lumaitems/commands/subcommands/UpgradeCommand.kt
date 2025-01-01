package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.guis.AstralUpgradeGui
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "upgrade",
    description = "Open the Astral Upgrade GUI",
    usage = "/<command> upgrade",
    permission = "lumaitems.command.upgrade",
    playerOnly = true,
    parent = CommandManager::class
)
class UpgradeCommand : SubCommand {

    companion object {
        val astralUpgradeGui = AstralUpgradeGui()
    }

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        sender.openInventory(astralUpgradeGui.getInventory())
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }

}