package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumacore.utility.Text
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.items.ItemFactory
import dev.lumas.lumaitems.util.extensions.send
import net.kyori.adventure.text.Component
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "tier",
    description = "Add a tier to an item",
    usage = "/<command> tier <text>",
    permission = "lumaitems.command.tier",
    playerOnly = true,
    parent = CommandManager::class
)
class AddTierCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        val item = sender.inventory.itemInMainHand
        if (item.type.isAir) {
            sender.send("You must be holding an item to add a tier!")
            return false
        }

        if (args.isEmpty()) {
            return false
        }

        val tier = args.joinToString(" ")

        item.editMeta { meta ->
            val lore: MutableList<Component> = meta?.lore() ?: mutableListOf()
            lore.addAll(Text.mmlNoItalic(ItemFactory.TIER_FORMAT.map { it.format(tier) }))
            meta.lore(lore)
        }

        sender.send("Successfully added tier to item.")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return listOf("<text>")
    }
}