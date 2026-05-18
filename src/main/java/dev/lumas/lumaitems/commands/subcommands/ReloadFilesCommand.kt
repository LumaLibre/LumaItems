package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.send
import org.bukkit.command.CommandSender

@Register(Autowire.SUBCOMMAND)
@CommandMeta(
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
        sender.send("Reloaded okaeri files.")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String> {
        return emptyList()
    }

}