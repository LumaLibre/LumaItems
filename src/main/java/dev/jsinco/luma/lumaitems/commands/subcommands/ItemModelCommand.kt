package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.util.MiniMessageUtil
import dev.jsinco.luma.lumaitems.util.Util
import io.papermc.paper.datacomponent.DataComponentTypes
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player


@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
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