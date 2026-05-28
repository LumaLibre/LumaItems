package dev.lumas.lumaitems.commands

import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierCommand
import dev.lumas.core.model.command.AbstractCommand
import dev.lumas.lumaitems.guis.AstralUpgradeGui
import dev.lumas.lumaitems.guis.MixerUpgradeGui
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Register(Autowire.COMMAND)
@CommandMeta(
    name = "upgrade",
    description = "Open the Astral upgrade gui",
    usage = "/<command>",
    playerOnly = true
)
class UpgradeCommand : BrigadierCommand() {

    companion object {
        private val astralUpgradeGui = AstralUpgradeGui()
    }

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument("target", optional = true) target: Player?) {
        val sender = src.sender
        val player: Player = if (target != null) {
            if (sender.hasPermission("lumaitems.command.upgrade.other")) {
                target
            } else {
                sender as? Player ?: return
            }
        } else {
            sender as? Player ?: return
        }

        player.openInventory(astralUpgradeGui.getInventory())
    }
}