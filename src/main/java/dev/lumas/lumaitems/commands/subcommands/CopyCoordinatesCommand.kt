package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.util.Util
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "copycoordinates",
    description = "Copy your current coordinates to clipboard",
    usage = "/<command> copycoordinates [includeYawPitch]",
    permission = "lumaitems.command.copycoordinates",
    parent = CommandManager::class,
    playerOnly = true
)
class CopyCoordinatesCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument(value = "includeYawPitch", optional = true) includeYawPitch: Boolean?) {
        val player = src.sender as Player
        val loc = player.location

        val coordinatesString = if (includeYawPitch == true) {
            "${player.world.name},${loc.blockX},${loc.blockY},${loc.blockZ},${loc.yaw},${loc.pitch}"
        } else {
            "${player.world.name},${loc.blockX},${loc.blockY},${loc.blockZ}"
        }

        val c = Util.getRandomColor()
        val comp = Component.text(coordinatesString)
            .clickEvent(ClickEvent.copyToClipboard(coordinatesString))
            .hoverEvent(Component.text("Click to copy"))
            .color(TextColor.color(c.red, c.green, c.blue))
        Text.msg(player, comp)
    }
}