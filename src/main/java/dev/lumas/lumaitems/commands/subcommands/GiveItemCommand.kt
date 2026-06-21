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
import dev.lumas.lumaitems.api.ItemManager
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.registry.StringIdentifier
import dev.lumas.lumaitems.util.Util
import dev.lumas.lumaitems.util.extensions.asComponent
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import java.util.concurrent.CompletableFuture
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "give",
    description = "Obtain a custom item",
    usage = "/<command> give <item> [target] [amount] [silent]",
    permission = "lumaitems.command.give",
    parent = CommandManager::class
)
class GiveItemCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(
        src: CommandSourceStack,
        @Argument(value = "item") itemName: String,
        @Argument(value = "target", optional = true) target: Player?,
        @Argument(value = "amount", optional = true) amount: Int?,
        @Argument(value = "drop", optional = true) drop: Boolean?,
        @Argument(value = "silent", optional = true) silent: Boolean?
    ) {
        val sender: CommandSender = src.sender

        val recipient: Player = target ?: (sender as? Player ?: run {
            Text.msg(sender, "<red>Must specify a target when running from console")
            return
        })

        val giveAmount = amount ?: 1
        val isSilent = silent ?: false

        if (itemName == "all") {
            for (customItem in ItemManager.getAllItems()) {
                if (customItem.isEmpty) continue
                Util.giveItem(recipient, customItem, drop ?: false)
            }
            recipient.send("You have been given all custom items!")
            return
        }

        val item = ItemManager.getItemByName(itemName) ?: ItemManager.getItemByKey(itemName) ?: run {
            Text.msg(sender, "<red>No item named $itemName")
            return
        }

        val maxStack = item.maxStackSize.coerceAtLeast(1)
        var remaining = giveAmount
        while (remaining > 0) {
            val give = remaining.coerceAtMost(maxStack)
            Util.giveItem(recipient, item.asQuantity(give), drop ?: false)
            remaining -= give
        }

        if (!isSilent) {
            Text.msg(recipient, item.itemMeta?.displayName()?.let {
                "<reset>You have been given</reset> <gold>${giveAmount}x</gold> ".asComponent().append(it)
            } ?: "???".asComponent())
        }
    }

    @Suggests("item")
    fun suggestItem(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        Registry.NAMED_CUSTOM_ITEMS.keySet(StringIdentifier::class).asSequence()
            .map { it.key() }
            .plus("all")
            .filter { it.lowercase().startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }
}