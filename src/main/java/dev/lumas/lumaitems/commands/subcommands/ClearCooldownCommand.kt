package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.send
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Register(Autowire.SUBCOMMAND)
@CommandMeta(
    name = "clearcooldown",
    description = "Clear cooldowns for a player",
    usage = "/<command> clearcooldown <player!>",
    permission = "lumaitems.command.clearcooldown",
    parent = CommandManager::class
)
class ClearCooldownCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val player = args.getOrNull(1)?.let { Bukkit.getPlayerExact(it) } ?: sender as? Player ?: run {
            sender.send("Player not found")
            return true
        }

        QuickTasks.removeNow(player.uniqueId)
        QuickTasks.removeAllFlags(player.uniqueId)
        QuickTasks.removeAllSpellCooldowns(player.uniqueId)
        sender.send("Cleared cooldown for player ${player.name}")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }
}