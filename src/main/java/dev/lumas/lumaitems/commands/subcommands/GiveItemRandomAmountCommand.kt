package dev.lumas.lumaitems.commands.subcommands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.annotation.Suggests
import dev.lumas.core.model.brigadier.ArgumentTypeProvider
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.api.ItemManager
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.providers.FreeFormStringProvider
import dev.lumas.lumaitems.commands.providers.NonNegativeIntProvider
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.registry.StringIdentifier
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "random",
    description = "Obtain a random amount of a custom item from a specified range",
    usage = "/<command> random <item> <target> <from> <until> [silent]",
    permission = "lumaitems.command.random",
    parent = CommandManager::class
)
class GiveItemRandomAmountCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(
        src: CommandSourceStack,
        @Argument("item") itemName: String,
        @Argument("target") target: Player,
        @Argument("from", provider = NonNegativeIntProvider::class) from: Int,
        @Argument("until", provider = NonNegativeIntProvider::class) until: Int,
        @Argument(value = "drop", optional = true) drop: Boolean?,
        @Argument(value = "silent", optional = true) silent: Boolean?
    ) {
        val sender: CommandSender = src.sender

        if (from > until) {
            sender.send("<red>Invalid range: 'from' ($from) must be ≤ 'until' ($until)")
            return
        }

        val item = ItemManager.getItemByName(itemName) ?: ItemManager.getItemByKey(itemName) ?: run {
            sender.send("<red>No item named $itemName")
            return
        }

        val amount = (from..until).random()

        if (amount < item.maxStackSize) {
            Util.giveItem(target, item.asQuantity(amount), drop ?: false)
        } else {
            for (i in 0 until amount step item.maxStackSize) {
                val stackAmount = minOf(item.maxStackSize, amount - i)
                Util.giveItem(target, item.asQuantity(stackAmount), drop ?: false)
            }
        }

        if (silent != true) {
            Text.msg(target, item.itemMeta?.displayName()?.let {
                "<reset>You have been given</reset> <gold>${amount}x</gold> ".asComponent().append(it)
            } ?: "???".asComponent())
        }
    }

    @Suggests("item")
    fun suggestItem(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        Registry.NAMED_CUSTOM_ITEMS.keySet(StringIdentifier::class).asSequence()
            .map { it.key() }
            .filter { it.lowercase().startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }


}