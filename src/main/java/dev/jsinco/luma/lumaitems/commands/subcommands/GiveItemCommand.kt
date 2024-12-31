package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.manager.ItemManager
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GiveItemCommand : SubCommand {

    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        val player = if (args.size >= 3) {
            plugin.server.getPlayerExact(args[2]) ?: return
        } else {
            sender as Player
        }


        val item = if (args[1] != "all") {
            ItemManager.getItemByName(args[1]) ?: ItemManager.getItemByKey(args[1]) ?: return
        } else {
            null
        }

        val amount = if (args.size >= 4) {
            args[3].toIntOrNull() ?: 1
        } else {
            1
        }


        if (item != null) {
            Util.giveItem(player, item.asQuantity(amount))
            if (!args.contains("-silent")) {
                MiniMessageUtil.msg(player, item.itemMeta?.displayName()?.let {
                    MiniMessageUtil.mm("<reset>You have been given</reset> <gold>${amount}x</gold> ").append(it) })
            }
        } else {
            for (customItem in ItemManager.getAllItems()) {
                Util.giveItem(player, customItem)
            }
            player.sendMessage("${Util.prefix} You have been given all custom items!")
        }
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return when (args.size) {
            2 -> {
                val list: MutableList<String> = ItemManager.customItemsByName.keys.toMutableList()
                list.add("all")
                list
            }
            3 -> {
                null
            }
            4 -> {
                listOf("<amount>")
            }
            else -> {
                listOf("-silent")
            }
        }
    }

    override fun permission(): String {
        return "lumaitems.command.give"
    }

    override fun playerOnly(): Boolean {
        return false
    }
}