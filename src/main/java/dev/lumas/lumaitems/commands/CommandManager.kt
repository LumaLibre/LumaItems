package dev.lumas.lumaitems.commands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.command.AbstractCommandManager
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.util.extensions.send
import org.bukkit.command.CommandSender

@Register(Autowire.COMMAND)
@CommandMeta(
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
            sender.send("Please provide a subcommand.")
            return false
        }
        return super.handle(sender, label, args)
    }
}