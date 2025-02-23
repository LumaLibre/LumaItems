package dev.jsinco.luma.lumaitems.commands

import dev.jsinco.luma.lumacore.manager.commands.AbstractCommandManager
import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumacore.utility.Text
import dev.jsinco.luma.lumaitems.LumaItems
import org.bukkit.command.CommandSender

@AutoRegister(RegisterType.COMMAND)
@CommandInfo(
    name = "lumaitems",
    aliases = ["li"],
    description = "Main command for LumaItems",
    usage = "/<command> <subcommand>",
    permission = "lumaitems.command",
    playerOnly = false
)
class CommandManager : AbstractCommandManager<LumaItems, SubCommand>(LumaItems.getInstance()) {

    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            Text.msg(sender, "Please provide a subcommand.")
            return false
        }
        return super.handle(sender, label, args)
    }
}