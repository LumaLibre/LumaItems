package dev.lumas.lumaitems.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierCommandManager
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "lumaitems",
    aliases = ["li"],
    description = "Main command for LumaItems",
    usage = "/<command> <subcommand>",
    permission = "lumaitems.command"
)
class CommandManager : BrigadierCommandManager() {

    override fun buildRootExecutor(root: LiteralArgumentBuilder<CommandSourceStack>, commands: Commands) {
        root.executes { ctx ->
            ctx.source.sender.send("Please provide a subcommand.")
            return@executes Command.SINGLE_SUCCESS
        }
    }
}