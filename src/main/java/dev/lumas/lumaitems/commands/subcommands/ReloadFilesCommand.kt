package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumacore.utility.Text
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.registry.Registry
import org.bukkit.command.CommandSender

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "reload",
    description = "Reloads all okaeri config files.",
    usage = "/<command> reload",
    permission = "lumaitems.command.reload",
    playerOnly = false,
    parent = CommandManager::class
)
class ReloadFilesCommand : SubCommand {

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        Registry.CONFIGS
            .map { it.value }
            .forEach { it.load(true) }
        Text.msg(sender, "Reloaded okaeri files.")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }

}