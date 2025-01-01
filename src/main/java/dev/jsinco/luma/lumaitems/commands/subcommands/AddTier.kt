package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "addtier",
    description = "Add a tier to an item",
    usage = "/<command> addtier <tier/gradient>",
    permission = "lumaitems.command.addtier",
    playerOnly = true,
    parent = CommandManager::class
)
class AddTier : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.sendMessage("${Util.prefix} You must be holding an item to add a tier")
            return false
        }

        if (args.isEmpty()) {
            sender.sendMessage("${Util.prefix} Invalid arguments. Usage: /lumaitems addtier <tier/gradient>")
            return false
        }

        val tier = args.joinToString(" ")
        val meta = item.itemMeta

        val lore: MutableList<String> = meta?.lore ?: mutableListOf()
        lore.add("")
        lore.add(Util.colorcode("&#EEE1D5&m       &r&#EEE1D5⋆⁺₊⋆ ★ ⋆⁺₊⋆&m       "))
        lore.add(Util.colorcode("&#EEE1D5Tier • $tier"))
        lore.add(Util.colorcode("&#EEE1D5&m       &r&#EEE1D5⋆⁺₊⋆ ★ ⋆⁺₊⋆&m       "))
        meta?.lore = lore
        item.itemMeta = meta
        sender.sendMessage("${Util.prefix} Successfully added tier to item")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return listOf("<tier/gradient>")
    }
}