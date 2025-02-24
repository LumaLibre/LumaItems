package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.manager.FileManager
import dev.jsinco.luma.lumaitems.relics.RelicCrafting
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "giveastral",
    description = "Obtain an Astral Set",
    usage = "/<command> giveastral <rarity!>",
    permission = "lumaitems.command.giveastral",
    playerOnly = true,
    parent = CommandManager::class
)
class GiveAstralCommand : SubCommand {

    private val file = FileManager("astral.yml").generateYamlFile()

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: return false
        if (args.size != 1) {
            player.sendMessage("Invalid arguments")
            return false
        }
        val items = RelicCrafting.getItemsFromClass(args[0])
        for (item in items) {
            player.inventory.addItem(item)
        }
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return file.getConfigurationSection("astral-orb-rarities")?.getKeys(false)?.toList()
    }

}