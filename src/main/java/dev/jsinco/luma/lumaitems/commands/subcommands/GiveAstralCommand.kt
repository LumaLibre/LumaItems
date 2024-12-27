package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.manager.FileManager
import dev.jsinco.luma.lumaitems.relics.RelicCrafting
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class GiveAstralCommand : SubCommand {

    private val file = FileManager("astral.yml").generateYamlFile()

    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        val player = sender as? Player ?: return
        if (args.size != 2) {
            player.sendMessage("Invalid arguments")
            return
        }
        val items = RelicCrafting.getItemsFromClass(args[1])
        for (item in items) {
            player.inventory.addItem(item)
        }
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return file.getConfigurationSection("astral-orb-rarities")?.getKeys(false)?.toList()
    }

    override fun permission(): String {
        return "lumaitems.command.giveastral"
    }

    override fun playerOnly(): Boolean {
        return true
    }
}