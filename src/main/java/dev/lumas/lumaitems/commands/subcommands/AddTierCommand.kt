package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.sendFormatted
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
class AddTierCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.sendFormatted("You must be holding an item to add a tier")
            return false
        }

        if (args.isEmpty()) {
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
        sender.sendFormatted("Successfully added tier to item")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return listOf("<tier/gradient>")
    }
}