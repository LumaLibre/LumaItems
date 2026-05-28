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
import dev.lumas.lumaitems.configuration.files.AstralYml
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.relics.RelicCrafting
import io.papermc.paper.command.brigadier.CommandSourceStack
import org.bukkit.entity.Player
import java.util.concurrent.CompletableFuture

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "giveastral",
    description = "Obtain an Astral Set",
    usage = "/<command> giveastral <rarity>",
    permission = "lumaitems.command.giveastral",
    playerOnly = true,
    parent = CommandManager::class
)
class GiveAstralCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument("rarity") rarity: String) {
        val player = src.sender as Player
        val items = RelicCrafting.getItemsFromClass(rarity)
        for (item in items) {
            player.inventory.addItem(item)
        }
    }

    @Suggests("rarity")
    fun suggestRarity(ctx: CommandContext<CommandSourceStack>, builder: SuggestionsBuilder): CompletableFuture<Suggestions> {
        val partial = builder.remaining.lowercase()
        Registry.CONFIGS.getOrThrow(AstralYml::class)
            .astralOrbRarities
            .keys
            .asSequence()
            .map { it.setClass.simpleName }
            .filterNotNull()
            .filter { it.lowercase().startsWith(partial) }
            .forEach(builder::suggest)
        return builder.buildFuture()
    }
}