package dev.lumas.lumaitems.commands

import dev.lumas.lumacore.manager.commands.AbstractCommandManager
import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.util.extensions.sendFormatted
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.util.extensions.sendFormatted
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
            sender.sendFormatted("Please provide a subcommand.")
            return false
        }
        return super.handle(sender, label, args)
    }
}