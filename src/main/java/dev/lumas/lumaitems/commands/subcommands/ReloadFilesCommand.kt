package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "reload",
    description = "Reloads all okaeri config files.",
    usage = "/<command> reload",
    permission = "lumaitems.command.reload",
    playerOnly = false,
    parent = CommandManager::class
)
class ReloadFilesCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack) {
        Registry.CONFIGS
            .map { it.value }
            .forEach { it.load(true) }
        src.sender.send("Reloaded okaeri files.")
    }

}