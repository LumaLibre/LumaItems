package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.obj.QuickTasks
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class ClearCooldownCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        val player = args.getOrNull(1)?.let { Bukkit.getPlayerExact(it) } ?: sender as? Player ?: run {
            MiniMessageUtil.msg(sender, "Player not found")
            return
        }

        QuickTasks.removeNow(player.uniqueId)
        MiniMessageUtil.msg(sender, "Cooldowns cleared for ${player.name}")
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return null
    }

    override fun permission(): String {
        return "lumaitems.command.clearcooldown"
    }

    override fun playerOnly(): Boolean {
        return false
    }
}