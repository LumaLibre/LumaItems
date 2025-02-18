package dev.jsinco.luma.lumaitems.commands.subcommands

import dev.jsinco.luma.lumaitems.LumaItems
import dev.jsinco.luma.lumaitems.commands.SubCommand
import dev.jsinco.luma.lumaitems.relics.RelicCrafting
import dev.jsinco.luma.lumaitems.util.Util
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class RelicCommand : SubCommand {
    override fun execute(plugin: LumaItems, sender: CommandSender, args: Array<out String>) {
        if (args.size < 2) {
            sender.sendMessage(Util.colorcode("${Util.legacyPrefix} Usage: /lumaitems relics <player> <shard|core|orb> <amount>?"))
            return
        }

        val player = Bukkit.getPlayerExact(args[1]) ?: return
        val amount: Int

        when (args[2]) {
            "shard" -> {
                amount = if (args.size < 4) 1 else args[3].toIntOrNull() ?: 1
                val item = RelicCrafting.relicShard.clone()
                item.amount = amount

                Util.giveItem(player, item)
            }
            "upgradecore" -> {
                amount = if (args.size < 4) 1 else args[3].toIntOrNull() ?: 1
                val item = RelicCrafting.astralUpgradeCore.clone()
                item.amount = amount

                Util.giveItem(player, item)
            }
            "core" -> {
                when (args[3]) {
                    "lunar" -> {
                        amount = if (args.size < 5) 1 else args[4].toIntOrNull() ?: 1
                        val item = RelicCrafting.lunarCore.clone()
                        item.amount = amount

                        Util.giveItem(player, item)
                    }
                    "astral" -> {
                        amount = if (args.size < 5) 1 else args[4].toIntOrNull() ?: 1
                        val item = RelicCrafting.astralCore.clone()
                        item.amount = amount

                        Util.giveItem(player, item)
                    }
                    else -> {
                        sender.sendMessage(Util.colorcode("${Util.legacyPrefix} Usage: /lumaitems relics <player> core <lunar|astral> <amount>?"))
                    }
                }
            }

            "orb" -> {
                when (args[3]) {
                    "lunar" -> {
                        amount = if (args.size < 5) 1 else args[4].toIntOrNull() ?: 1
                        val item = RelicCrafting.lunarOrb.clone()
                        item.amount = amount

                        Util.giveItem(player, item)
                    }
                    "astral" -> {
                        amount = if (args.size < 5) 1 else args[4].toIntOrNull() ?: 1
                        val item = RelicCrafting.astralOrb.clone()
                        item.amount = amount

                        Util.giveItem(player, item)
                    }
                    else -> {
                        sender.sendMessage(Util.colorcode("${Util.legacyPrefix} Usage: /lumaitems relics <player> orb <lunar|astral> <amount>?"))
                    }
                }
            }
        }
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        when (args.size) {
            3 -> {
                return mutableListOf("shard", "core", "orb", "upgradecore")
            }

            4 -> {
                if (args[2] != "shard") {
                    return mutableListOf("lunar", "astral")
                }
                return mutableListOf("<amount>")

            }

            5 -> {
                return mutableListOf("<amount>")
            }
        }
        return null
    }

    override fun permission(): String {
        return "lumaitems.command.relic"
    }

    override fun playerOnly(): Boolean {
        return false
    }
}