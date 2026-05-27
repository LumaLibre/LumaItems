package dev.lumas.lumaitems.commands

import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierCommand
import dev.lumas.lumaitems.guis.MixerUpgradeGui
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "mixer",
    description = "Open the mixer upgrade gui",
    usage = "/<command>"
)
class MixerCommand : BrigadierCommand() {

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument("target", optional = true) target: Player?) {
        val sender = src.sender
        val player: Player = if (target != null) {
            if (sender.hasPermission("lumaitems.command.mixer.other")) {
                target
            } else {
                sender as? Player ?: return
            }
        } else {
            sender as? Player ?: return
        }

        val gui = MixerUpgradeGui()
        player.openInventory(gui.inventory)
    }
}