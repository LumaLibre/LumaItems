package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.util.extensions.namespacedKey
import dev.lumas.lumaitems.util.extensions.send
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "addkey",
    description = "Add a key to an item",
    usage = "/<command> addkey <key>",
    permission = "lumaitems.command.addkey",
    playerOnly = true,
    parent = CommandManager::class
)
class AddKeyCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: return false
        val item = player.inventory.itemInMainHand

        val key = args.getOrNull(0) ?: return false

        item.editPersistentDataContainer {
            it.set(key.namespacedKey(), PersistentDataType.SHORT, 1)
        }
        player.send("Added key: $key")
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String?>? {
        return null
    }
}