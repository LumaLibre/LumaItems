package dev.lumas.lumaitems.commands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.command.AbstractCommand
import dev.lumas.lumaitems.guis.AstralUpgradeGui
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Register(Autowire.COMMAND)
@CommandMeta(
    name = "upgrade",
    description = "Open the Astral upgrade gui",
    usage = "/<command>",
    playerOnly = true
)
class UpgradeCommand : AbstractCommand() {

    companion object {
        private val astralUpgradeGui = AstralUpgradeGui()
    }

    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        sender.openInventory(astralUpgradeGui.getInventory())
        return true
    }

    override fun handleTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String>? {
        return null
    }
}