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
import dev.lumas.lumaitems.commands.CommandManager
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "itemmodel",
    description = "Change model of an item",
    usage = "/<command> itemmodel <model>",
    permission = "lumaitems.command.itemmodel",
    parent = CommandManager::class,
    playerOnly = true
)
class ItemModelCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument("model") model: String) {
        val player = src.sender as Player
        val itemInHand = player.inventory.itemInMainHand
        itemInHand.setData(DataComponentTypes.ITEM_MODEL, NamespacedKey.minecraft(model))
    }

    @Suggests("model")
    fun suggestModel(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        Material.entries.asSequence()
            .map { it.name.lowercase() }
            .filter { it.startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }
}