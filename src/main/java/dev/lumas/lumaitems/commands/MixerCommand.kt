package dev.lumas.lumaitems.commands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.command.AbstractCommand
import dev.lumas.lumaitems.guis.MixerUpgradeGui
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Register(Autowire.COMMAND)
@CommandMeta(
    name = "mixer",
    description = "Open the mixer upgrade gui",
    usage = "/<command>",
    playerOnly = true
)
class MixerCommand : AbstractCommand() {
    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val gui = MixerUpgradeGui()
        val player = sender as Player
        player.openInventory(gui.getInventory())
        return true
    }

    override fun handleTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
        return emptyList()
    }
}