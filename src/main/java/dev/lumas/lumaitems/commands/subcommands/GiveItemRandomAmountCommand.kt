package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.util.Text
import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.api.ItemManager
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.registry.StringIdentifier
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.getNextIntArgument
import dev.lumas.lumaitems.util.extensions.send
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "random",
    description = "Obtain a random amount of a custom item from a specified range",
    usage = "/<command> random <item!> <player!> <from!> <until!> [-silent]",
    permission = "lumaitems.command.random",
    playerOnly = false,
    parent = CommandManager::class
)
class GiveItemRandomAmountCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val itemName = args.getOrNull(0) ?: return false
        val target = args.getOrNull(1)?.let { plugin.server.getPlayerExact(it) } ?: sender as? Player ?: return false
        val from = args.getOrNull(2)?.toIntOrNull() ?: return false
        val until = args.getOrNull(3)?.toIntOrNull() ?: return false

        if (from < 0 || until < 0 || from > until) {
            sender.send("Invalid range! Ensure that 'from' and 'until' are non-negative integers and that 'from' is less than or equal to 'until'.")
            return false
        }

        val item = ItemManager.getItemByName(itemName) ?: ItemManager.getItemByKey(itemName) ?: return false

        val amount = (from..until).random()

        if (amount < item.maxStackSize) {
            target.give(item.asQuantity(amount))
        } else {
            for (i in 0 until amount step item.maxStackSize) {
                val stackAmount = minOf(item.maxStackSize, amount - i)
                target.give(item.asQuantity(stackAmount))
            }
        }

        if (!args.contains("-silent")) {
            Text.msg(target, item.itemMeta?.displayName()?.let {
                "<reset>You have been given</reset> <gold>${amount}x</gold> ".asComponent().append(it) } ?: "???".asComponent())
        }
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String?>? {
        return when (args.size) {
            1 -> Registry.NAMED_CUSTOM_ITEMS.keySet(StringIdentifier::class).map { it.key() }
            2 -> null
            3 -> {
                val arg = args.getOrNull(2) ?: return emptyList()
                arg.getNextIntArgument()
            }
            4 -> {
                val arg = args.getOrNull(3) ?: return emptyList()
                arg.getNextIntArgument()
            }
            else -> listOf("-silent")
        }
    }
}