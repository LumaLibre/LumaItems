package dev.lumas.lumaitems.commands.subcommands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.relics.RelicCrafting
import dev.lumas.lumaitems.util.Util
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "relic",
    description = "Obtain a relic item",
    usage = "/<command> relic <player> <shard|core|orb|upgradecore> [lunar|astral] [amount]",
    permission = "lumaitems.command.relic",
    parent = CommandManager::class
)
class RelicCommand : BrigadierSubCommand {

    override fun buildTree(
        builder: LiteralArgumentBuilder<CommandSourceStack>,
        commands: Commands
    ): LiteralArgumentBuilder<CommandSourceStack> {
        return builder.then(
            Commands.argument("player", ArgumentTypes.player())
                // shard <player> [amount]
                .then(Commands.literal("shard")
                    .executes { ctx -> give(ctx, RelicCrafting.relicShard, 1) }
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { ctx -> give(ctx, RelicCrafting.relicShard, IntegerArgumentType.getInteger(ctx, "amount")) }
                    )
                )
                // upgradecore <player> [amount]
                .then(Commands.literal("upgradecore")
                    .executes { ctx -> give(ctx, RelicCrafting.astralUpgradeCore, 1) }
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { ctx -> give(ctx, RelicCrafting.astralUpgradeCore, IntegerArgumentType.getInteger(ctx, "amount")) }
                    )
                )
                // core <player> <lunar|astral> [amount]
                .then(Commands.literal("core")
                    .then(variantBranch("lunar", RelicCrafting.lunarCore))
                    .then(variantBranch("astral", RelicCrafting.astralCore))
                )
                // orb <player> <lunar|astral> [amount]
                .then(Commands.literal("orb")
                    .then(variantBranch("lunar", RelicCrafting.lunarOrb))
                    .then(variantBranch("astral", RelicCrafting.astralOrb))
                )
        )
    }

    private fun variantBranch(variantName: String, baseItem: ItemStack): LiteralArgumentBuilder<CommandSourceStack> {
        return Commands.literal(variantName)
            .executes { ctx -> give(ctx, baseItem, 1) }
            .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                .executes { ctx -> give(ctx, baseItem, IntegerArgumentType.getInteger(ctx, "amount")) }
            )
    }

    private fun give(ctx: CommandContext<CommandSourceStack>, baseItem: ItemStack, amount: Int): Int {
        val resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
        val resolved = resolver.resolve(ctx.source)
        if (resolved.isEmpty()) {
            return 0
        }
        val target: Player = resolved.first()
        val item = baseItem.clone()
        item.amount = amount
        Util.giveItem(target, item)
        return Command.SINGLE_SUCCESS
    }
}