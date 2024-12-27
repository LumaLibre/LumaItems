package dev.jsinco.luma.commands.subcommands

import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.commands.SubCommand
import dev.jsinco.luma.util.Util
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class AddTier : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        sender as Player
        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.sendMessage("${Util.prefix} You must be holding an item to add a tier")
            return
        }

        if (args.size < 2) {
            sender.sendMessage("${Util.prefix} Invalid arguments. Usage: /lumaitems addtier <tier/gradient>")
            return
        }

        val tier = args.joinToString(" ").replace(args[0], "").trim()
        val meta = item.itemMeta

        val lore: MutableList<String> = meta?.lore ?: mutableListOf()
        lore.add("")
        lore.add(Util.colorcode("&#EEE1D5&m       &r&#EEE1D5⋆⁺₊⋆ ★ ⋆⁺₊⋆&m       "))
        lore.add(Util.colorcode("&#EEE1D5Tier • $tier"))
        lore.add(Util.colorcode("&#EEE1D5&m       &r&#EEE1D5⋆⁺₊⋆ ★ ⋆⁺₊⋆&m       "))
        meta?.lore = lore
        item.itemMeta = meta
        sender.sendMessage("${Util.prefix} Successfully added tier to item")
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return listOf("<tier/gradient>")
    }

    override fun permission(): String? {
        return "lumaitems.command.addtier"
    }

    override fun playerOnly(): Boolean {
        return true
    }
}