package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.manager.ItemManager
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
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
        return true
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
}