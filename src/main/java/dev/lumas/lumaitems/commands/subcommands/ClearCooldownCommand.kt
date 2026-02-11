package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.sendFormatted
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "clearcooldown",
    description = "Clear cooldowns for a player",
    usage = "/<command> clearcooldown <player!>",
    permission = "lumaitems.command.clearcooldown",
    parent = CommandManager::class
)
class ClearCooldownCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val player = args.getOrNull(1)?.let { Bukkit.getPlayerExact(it) } ?: sender as? Player ?: run {
            sender.sendFormatted("Player not found")
            return true
        }

        QuickTasks.removeNow(player.uniqueId)
        QuickTasks.removeAllFlags(player.uniqueId)
        QuickTasks.removeAllSpellCooldowns(player.uniqueId)
        sender.sendFormatted("Cleared cooldown for player ${player.name}")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }
}