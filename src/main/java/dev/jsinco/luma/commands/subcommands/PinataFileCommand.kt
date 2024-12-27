package dev.jsinco.luma.commands.subcommands

import dev.jsinco.luma.manager.FileManager
import dev.jsinco.luma.LumaItems
import dev.jsinco.luma.commands.SubCommand
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class PinataFileCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        val player = sender as Player
        val fileManager = FileManager("saves/pinata.yml")
        fileManager.generateFile()
        val pinataFile = fileManager.getFileYaml()


        val item = player.inventory.itemInMainHand
        val name = if (item.itemMeta?.hasDisplayName() == true) {
            ChatColor.stripColor(item.itemMeta?.displayName)!!.replace(" ", "_").lowercase()
        } else {
            item.type.name.lowercase()
        }
        if (args[1] == "add") {
            val type = if (args[2] == "rare") {
                "rare-items"
            } else {
                "items"
            }
            pinataFile.set("$type.$name", item)
        } else {
            pinataFile.set("items.$name", null)
            pinataFile.set("rare-items.$name", null)
        }
        fileManager.saveFileYaml()

        player.sendMessage("Performed command")

    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        if (args.size == 2) {
            return listOf("add", "remove")
        }

        when (args[1]) {
            "add" -> {
                return listOf("rare", "normal")
            }
        }
        return null
    }

    override fun permission(): String {
        return "lumaitems.command.pinatafile"
    }

    override fun playerOnly(): Boolean {
        return true
    }
}