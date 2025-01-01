package dev.jsinco.luma.lumaitems.commands

import dev.jsinco.luma.lumacore.manager.commands.AbstractCommandManager
import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import org.bukkit.command.CommandSender

@AutoRegister(RegisterType.COMMAND)
@CommandInfo(
    name = "lumaitems",
    description = "Main command for LumaItems",
    usage = "/<command> <subcommand",
    permission = "lumaitems.command",
    playerOnly = false
)
class CommandManager : AbstractCommandManager<LumaItems, SubCommand>(LumaItems.getInstance()) {

    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            MiniMessageUtil.msg(sender, "&cPlease provide a subcommand.")
            return false
        }
        return super.handle(sender, label, args)
    }

    override fun handleTabComplete(sender: CommandSender, label: String, args: Array<out String>): MutableList<String>? {
        return super.handleTabComplete(sender, label, args)
    }
}