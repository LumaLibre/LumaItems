package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "copycoordinates",
    description = "Copy your current coordinates",
    usage = "/<command> copycoordinates",
    permission = "lumaitems.command.copycoordinates",
    playerOnly = true,
    parent = CommandManager::class
)
class CopyCoordinates : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        val coordinatesString = "${sender.world.name},${sender.location.blockX},${sender.location.blockY},${sender.location.blockZ}"
        MiniMessageUtil.msg(sender, Component.text(coordinatesString).clickEvent(ClickEvent.copyToClipboard(coordinatesString)).hoverEvent(Component.text("Click to copy")))
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }

}