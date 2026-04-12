package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.util.Text
import dev.lumas.lumacore.manager.commands.CommandInfo
import dev.lumas.lumacore.manager.modules.AutoRegister
import dev.lumas.lumacore.manager.modules.RegisterType
import dev.lumas.lumaitems.LumaItems
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.SubCommand
import dev.lumas.lumaitems.items.misc.jobs.JobsBoosterItem
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.asEnum
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.SUBCOMMAND)
@CommandInfo(
    name = "booster",
    description = "Obtain a jobs booster item",
    usage = "/<command> booster <exp|money|exp_and_money!> <value!> <duration!> <player?>",
    permission = "lumaitems.command.booster",
    playerOnly = false,
    parent = CommandManager::class
)
class JobsBoosterItemCommand : SubCommand {

    override fun execute(plugin: LumaItems, sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val type: JobsBoosterItem.BoostType = args.getOrNull(0)?.asEnum(JobsBoosterItem.BoostType::class.java) ?: return false
        val multiplier: Double = args.getOrNull(1)?.let { multiplierToDouble(it) } ?: return false
        val duration: String = args.getOrNull(2) ?: return false
        val target: Player = args.getOrNull(3)?.let { Bukkit.getPlayerExact(it) } ?: sender as? Player ?: return false

        val jobsBoosterItem = Registry.CUSTOM_ITEMS.getOrThrow(JobsBoosterItem::class)

        val item = jobsBoosterItem.createBooster(type, duration, multiplier)

        target.give(item)

        Text.msg(target, item.itemMeta?.displayName()?.let {
            "<reset>You have been given</reset> <gold>1x</gold> ".asComponent().append(it) } ?: "???".asComponent())
        return true
    }

    override fun tabComplete(plugin: LumaItems, sender: CommandSender, args: Array<out String>): List<String>? {
        return when (args.size) {
            1 -> listOf("exp", "money", "exp_and_money")
            2 ->listOf("25%", "50%", "2x")
            3 -> listOf("30m", "1h", "4h", "12h")
            4 -> Bukkit.getOnlinePlayers().map { it.name }.toList()

            else -> emptyList()
        }
    }

    private fun multiplierToDouble(multiplier: String): Double {
        // when % at the end, convert to decimal: 25% = 0.25
        // when x at the end, convert to integer - 1: 2x = 1

        return when {
            multiplier.endsWith("%") -> {
                multiplier.dropLast(1).toDouble() / 100.0
            }
            multiplier.endsWith("x") -> {
                multiplier.dropLast(1).toDouble() - 1.0
            }
            else -> {
                throw IllegalArgumentException("Invalid multiplier format")
            }
        }
    }
}