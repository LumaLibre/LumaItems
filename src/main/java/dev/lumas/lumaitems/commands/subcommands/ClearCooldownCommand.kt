package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.util.extensions.QuickTasks
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "clearcooldown",
    description = "Clear cooldowns for one or more players",
    usage = "/<command> clearcooldown [targets]",
    permission = "lumaitems.command.clearcooldown",
    parent = CommandManager::class
)
class ClearCooldownCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument(value = "targets", optional = true) targets: List<@JvmSuppressWildcards Player>?) {
        val sender = src.sender

        val players: List<Player> = when {
            !targets.isNullOrEmpty() -> targets
            sender is Player -> listOf(sender)
            else -> {
                sender.send("Must specify at least one target.")
                return
            }
        }

        for (player in players) {
            QuickTasks.removeNow(player.uniqueId)
            QuickTasks.removeAllFlags(player.uniqueId)
            QuickTasks.removeAllSpellCooldowns(player.uniqueId)
        }

        when (players.size) {
            1 -> sender.send("Cleared cooldown for ${players[0].name}")
            else -> sender.send("Cleared cooldowns for ${players.size} players")
        }
    }
}