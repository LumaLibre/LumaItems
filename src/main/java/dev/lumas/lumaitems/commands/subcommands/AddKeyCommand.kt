package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "addkey",
    description = "Add a key to an item",
    usage = "/<command> addkey <key>",
    permission = "lumaitems.command.addkey",
    playerOnly = true,
    parent = CommandManager::class
)
class AddKeyCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument("key") key: String) {
        val player = src.sender as Player
        val item = player.inventory.itemInMainHand

        item.editPersistentDataContainer {
            it.set(key.namespacedKey(), PersistentDataType.SHORT, 1)
        }
        player.send("Added key: $key")
    }
}