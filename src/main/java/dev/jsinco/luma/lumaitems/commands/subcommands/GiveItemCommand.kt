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
        val player = if (args.size == 3) {
            plugin.server.getPlayerExact(args[2]) ?: return
        } else {
            sender as Player
        }


        val item = if (args[1] != "all") {
            ItemManager.getItemByName(args[1]) ?: ItemManager.getItemByKey(args[1]) ?: return
        } else {
            null
        }


        if (item != null) {
            Util.giveItem(player, item)
            MiniMessageUtil.msg(player, item.itemMeta?.displayName()?.let { MiniMessageUtil.mm("You have been given a custom item! <dark_gray>(</dark_gray>").append(it).append(MiniMessageUtil.mm("<dark_gray>)</dark_gray>")) })
        } else {
            for (customItem in ItemManager.getAllItems()) {
                Util.giveItem(player, customItem)
            }
            player.sendMessage("${Util.prefix} You have been given all custom items!")
        }
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        when (args.size) {
            2 -> {
                val list: MutableList<String> = ItemManager.customItemsByName.keys.toMutableList()
                list.add("all")
                return list
            }
        }
        return null
    }

    override fun permission(): String {
        return "lumaitems.command.give"
    }

    override fun playerOnly(): Boolean {
        return false
    }
}