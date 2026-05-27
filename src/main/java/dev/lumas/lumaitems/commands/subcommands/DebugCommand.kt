package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.setRemainingHealth
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "debug",
    description = "Debug command",
    usage = "/<command> debug",
    permission = "lumaitems.command.debug",
    playerOnly = false,
    parent = CommandManager::class
)
class DebugCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack) {
        val player = src.sender as Player
        val item = player.inventory.itemInMainHand
        item.setRemainingHealth(1)
        for ((index, item) in player.inventory.contents.withIndex()) {
            player.send("Slot $index: ${item?.type ?: "null"}")
        }
    }

}
