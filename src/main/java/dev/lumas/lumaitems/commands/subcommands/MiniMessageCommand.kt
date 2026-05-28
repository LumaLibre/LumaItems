package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.providers.GreedyStringProvider
import io.papermc.paper.command.brigadier.CommandSourceStack

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "mm",
    aliases = ["minimessage"],
    description = "Minimessage some text",
    usage = "/<command> mm <text>",
    permission = "lumaitems.command.minimessage",
    parent = CommandManager::class
)
class MiniMessageCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument(value = "text", provider = GreedyStringProvider::class) text: String) {
        src.sender.sendMessage(Text.mm(text))
    }


}