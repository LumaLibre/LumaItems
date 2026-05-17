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
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.send
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "give",
    description = "Obtain a custom item",
    usage = "/<command> give <item!> <player?> <amount?> [-silent]",
    permission = "lumaitems.command.give",
    playerOnly = false,
    parent = CommandManager::class
)
class GiveItemCommand : SubCommand {

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) return false
        val item = if (args[0] != "all") {
            ItemManager.getItemByName(args[0]) ?: ItemManager.getItemByKey(args[0]) ?: return false
        } else {
            null
        }


        val player = if (args.size >= 2) {
            plugin.server.getPlayerExact(args[1]) ?: return false
        } else {
            sender as Player
        }


        val amount = if (args.size >= 3) {
            args[2].toIntOrNull() ?: 1
        } else {
            1
        }


        if (item != null) {
            val maxStack = item.maxStackSize.coerceAtLeast(1)
            var remaining = amount
            while (remaining > 0) {
                val give = remaining.coerceAtMost(maxStack)
                Util.giveItem(player, item.asQuantity(give))
                remaining -= give
            }
            if (!args.contains("-silent")) {
                Text.msg(player, item.itemMeta?.displayName()?.let {
                    "<reset>You have been given</reset> <gold>${amount}x</gold> ".asComponent().append(it)
                } ?: "???".asComponent())
            }
        } else {
            for (customItem in ItemManager.getAllItems()) {
                if (customItem.isEmpty) continue
                Util.giveItem(player, customItem)
            }
            player.send("You have been given all custom items!")
        }
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return when (args.size) {
            1 -> {
                val list: MutableList<String> = Registry.NAMED_CUSTOM_ITEMS.keySet(StringIdentifier::class).map { it.key() }.toMutableList()
                list.add("all")
                list
            }
            2 -> {
                null
            }
            3 -> {
                listOf("<amount>")
            }
            else -> {
                listOf("-silent")
            }
        }
    }
}