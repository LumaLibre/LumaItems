package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.util.Text
import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import org.bukkit.command.CommandSender

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "mm",
    aliases = ["minimessage"],
    description = "Minimessage some text",
    usage = "/<command> mm <text>",
    permission = "lumaitems.command.minimessage",
    playerOnly = false,
    parent = CommandManager::class
)
class MiniMessageCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val text = args.joinToString(" ")
        sender.sendMessage(Text.mm(text))
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }
}