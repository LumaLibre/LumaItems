package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.setRemainingHealth
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Register(Autowire.SUBCOMMAND)
@CommandMeta(
    name = "debug",
    description = "Debug command",
    usage = "/<command> debug",
    permission = "lumaitems.command.debug",
    playerOnly = false,
    parent = CommandManager::class
)
class DebugCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val player = sender as Player
        val item = player.inventory.itemInMainHand
        item.setRemainingHealth(1)
        for ((index, item) in player.inventory.contents.withIndex()) {
            player.send("Slot $index: ${item?.type ?: "null"}")
        }
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String?>? {
        return null
    }

}
