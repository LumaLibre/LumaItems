package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


@Register(Autowire.SUBCOMMAND)
@CommandMeta(
    name = "itemmodel",
    description = "Change model of an item",
    usage = "/<command> itemmodel",
    permission = "lumaitems.command.itemmodel",
    parent = CommandManager::class,
    playerOnly = true
)
class ItemModelCommand : SubCommand {

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        sender as Player
        val itemInHand = sender.inventory.itemInMainHand
        itemInHand.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(args[0]))
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String> {
        return Material.entries.map { it.name.lowercase() }
    }
}