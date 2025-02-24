package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.CommandManager
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.relics.RelicCrafting
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "relic",
    description = "Obtain a relic item",
    usage = "/<command> relic <player> <shard|core|orb|upgradecore!> <lunar|astral?> <amount?>",
    permission = "lumaitems.command.upgrade",
    playerOnly = false,
    parent = CommandManager::class
)
class RelicCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        if (args.isEmpty()) {
            return false
        }

        val player = Bukkit.getPlayerExact(args[0]) ?: return false
        val amount: Int

        when (args[1]) {
            "shard" -> {
                amount = if (args.size < 3) 1 else args[2].toIntOrNull() ?: 1
                val item = RelicCrafting.relicShard.clone()
                item.amount = amount

                Util.giveItem(player, item)
            }
            "upgradecore" -> {
                amount = if (args.size < 3) 1 else args[2].toIntOrNull() ?: 1
                val item = RelicCrafting.astralUpgradeCore.clone()
                item.amount = amount

                Util.giveItem(player, item)
            }
            "core" -> {
                when (args[2]) {
                    "lunar" -> {
                        amount = if (args.size < 4) 1 else args[3].toIntOrNull() ?: 1
                        val item = RelicCrafting.lunarCore.clone()
                        item.amount = amount

                        Util.giveItem(player, item)
                    }
                    "astral" -> {
                        amount = if (args.size < 4) 1 else args[3].toIntOrNull() ?: 1
                        val item = RelicCrafting.astralCore.clone()
                        item.amount = amount

                        Util.giveItem(player, item)
                    }
                    else -> {
                        return false
                    }
                }
            }

            "orb" -> {
                when (args[2]) {
                    "lunar" -> {
                        amount = if (args.size < 4) 1 else args[3].toIntOrNull() ?: 1
                        val item = RelicCrafting.lunarOrb.clone()
                        item.amount = amount

                        Util.giveItem(player, item)
                    }
                    "astral" -> {
                        amount = if (args.size < 4) 1 else args[3].toIntOrNull() ?: 1
                        val item = RelicCrafting.astralOrb.clone()
                        item.amount = amount

                        Util.giveItem(player, item)
                    }
                    else -> {
                        return false
                    }
                }
            }
        }
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        when (args.size) {
            2 -> {
                return mutableListOf("shard", "core", "orb", "upgradecore")
            }

            3 -> {
                if (args[2] != "shard") {
                    return mutableListOf("lunar", "astral")
                }
                return mutableListOf("<amount>")

            }

            4 -> {
                return mutableListOf("<amount>")
            }
        }
        return null
    }
}