package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
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
            MiniMessageUtil.msg(sender, "Player not found")
            return true
        }

        QuickTasks.removeNow(player.uniqueId)
        MiniMessageUtil.msg(sender, "Cooldowns cleared for ${player.name}")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }
}