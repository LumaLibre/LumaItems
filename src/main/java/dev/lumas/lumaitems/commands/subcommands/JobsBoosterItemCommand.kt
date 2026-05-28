package dev.lumas.lumaitems.commands.subcommands

import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.annotation.Suggests
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.items.misc.jobs.JobsBoosterItem
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.asEnum
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "booster",
    description = "Obtain a jobs booster item",
    usage = "/<command> booster <type> <value> <duration> [target]",
    permission = "lumaitems.command.booster",
    parent = CommandManager::class
)
class JobsBoosterItemCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(
        src: CommandSourceStack,
        @Argument("type") typeName: String,
        @Argument("value") value: String,
        @Argument("duration") duration: String,
        @Argument(value = "target", optional = true) target: Player?
    ) {
        val type = typeName.asEnum(JobsBoosterItem.BoostType::class.java)
        if (type == null) {
            Text.msg(src.sender, "<red>Invalid type: $typeName")
            return
        }

        val multiplier = try {
            multiplierToDouble(value)
        } catch (e: IllegalArgumentException) {
            Text.msg(src.sender, "<red>Invalid multiplier: $value (expected like '25%' or '2x')")
            return
        }

        val recipient: Player = target ?: (src.sender as? Player ?: run {
            Text.msg(src.sender, "<red>Must specify a target when running from console")
            return
        })

        val jobsBoosterItem = Registry.CUSTOM_ITEMS.getOrThrow(JobsBoosterItem::class)
        val item = jobsBoosterItem.createBooster(type, duration, multiplier)
        recipient.give(item)

        Text.msg(recipient, item.itemMeta?.displayName()?.let {
            "<reset>You have been given</reset> <gold>1x</gold> ".asComponent().append(it)
        } ?: "???".asComponent())
    }

    @Suggests("type")
    fun suggestType(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        JobsBoosterItem.BoostType.entries
            .map { it.name.lowercase() }
            .filter { it.startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    @Suggests("value")
    fun suggestValue(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        listOf("25%", "50%", "2x")
            .filter { it.lowercase().startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    @Suggests("duration")
    fun suggestDuration(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        listOf("30m", "1h", "4h", "12h")
            .filter { it.startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }

    private fun multiplierToDouble(multiplier: String): Double = when {
        multiplier.endsWith("%") -> multiplier.dropLast(1).toDouble() / 100.0
        multiplier.endsWith("x") -> multiplier.dropLast(1).toDouble() - 1.0
        else -> throw IllegalArgumentException("Invalid multiplier format")
    }
}