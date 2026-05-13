package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.shapes.Sphere
import dev.lumas.lumaitems.util.extensions.send
import dev.lumas.lumaitems.util.extensions.setRemainingHealth
import java.util.function.Consumer
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
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
