package dev.lumas.lumaitems.commands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.command.AbstractCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.items.misc.nests.RepairTokenTier1Item
import dev.lumas.lumaitems.items.misc.nests.RepairTokenTier2Item
import dev.lumas.lumaitems.items.misc.nests.RepairTokenTier3Item
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.asComponent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Register(Autowire.COMMAND)
@CommandMeta(
    name = "repairtoken",
    description = "Legacy repair token command from JetsRepairTokens",
    usage = "/<command> <give|giveall> <Repair1|Repair2|Repair3> <amount?>",
    aliases = ["rt", "jetsrepairtokens"],
    permission = "lumaitems.command.repairtoken",
)
class RepairTokenCommand : AbstractCommand() {

    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val arg1 = args.getOrNull(0) ?: return false

        when(arg1.lowercase()) {
            "give" -> {
                val player = args.getOrNull(1)?.let { Bukkit.getPlayerExact(it) } ?: return false
                val type = args.getOrNull(2)?.lowercase() ?: return false
                val quantity = args.getOrNull(3)?.toIntOrNull() ?: 1
                giveRepairGem(player, type, quantity)
            }

            "giveall" -> {
                val type = args.getOrNull(1)?.lowercase() ?: return false
                val quantity = args.getOrNull(2)?.toIntOrNull() ?: 1
                for (player in Bukkit.getOnlinePlayers()) {
                    giveRepairGem(player, type, quantity)
                }
            }
        }

        return true
    }

    override fun handleTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String>? {
        return when (args.size) {
            1 -> listOf("give", "giveall")
            2 -> {
                if (args[0].lowercase() == "give") {
                    Bukkit.getOnlinePlayers().mapNotNull { it.name }
                } else {
                    listOf("Repair1", "Repair2", "Repair3")
                }
            }
            3 -> {
                if (args[0].lowercase() == "give") {
                    listOf("Repair1", "Repair2", "Repair3")
                } else {
                    null
                }
            }
            4 ->  {
                if (args[0].lowercase() == "give") {
                    listOf("<amount>")
                } else {
                    null
                }
            }
            else -> null
        }
    }

    private fun giveRepairGem(player: Player, type: String, quantity: Int) {
        val item = getRepairToken(type, quantity)
        player.give(item)
        Text.msg(player, item.itemMeta?.displayName()?.let {
            "<reset>You have been given</reset> <gold>${quantity}x</gold> ".asComponent().append(it)
        } ?: "???".asComponent())
    }

    private fun getRepairToken(name: String, quantity: Int): ItemStack {
        val itemClass = when(name.lowercase()) {
            "repair1" -> RepairTokenTier1Item::class
            "repair2" -> RepairTokenTier2Item::class
            "repair3" -> RepairTokenTier3Item::class
            else -> RepairTokenTier1Item::class
        }

        val customItem = Registry.CUSTOM_ITEMS.getOrThrow(itemClass)
        return customItem.createItem().second.asQuantity(quantity)
    }

}