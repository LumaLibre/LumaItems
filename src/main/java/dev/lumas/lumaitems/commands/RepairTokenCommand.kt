package dev.lumas.lumaitems.commands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.items.misc.nests.RepairTokenTier1Item
import dev.lumas.lumaitems.items.misc.nests.RepairTokenTier2Item
import dev.lumas.lumaitems.items.misc.nests.RepairTokenTier3Item
import dev.lumas.lumaitems.registry.Registry
import dev.lumas.lumaitems.util.extensions.asComponent
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import io.papermc.paper.command.brigadier.argument.ArgumentTypes
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "repairtoken",
    description = "Legacy repair token command from JetsRepairTokens",
    usage = "/<command> <give|giveall> ...",
    aliases = ["rt", "jetsrepairtokens"],
    permission = "lumaitems.command.repairtoken"
)
class RepairTokenCommand : BrigadierCommand() {

    override fun buildTree(builder: LiteralArgumentBuilder<CommandSourceStack>, commands: Commands): LiteralArgumentBuilder<CommandSourceStack> {
        return builder
            // /repairtoken give <player> <type> [amount]
            .then(Commands.literal("give")
                .then(Commands.argument("player", ArgumentTypes.player())
                    .then(typeArg { ctx, type -> giveOne(ctx, type, 1) }
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                            .executes { ctx ->
                                val type = ctx.getArgument("type", String::class.java)
                                val amount = IntegerArgumentType.getInteger(ctx, "amount")
                                giveOne(ctx, type, amount)
                            }
                        )
                    )
                )
            )
            // /repairtoken giveall <type> [amount]
            .then(Commands.literal("giveall")
                .then(typeArg { ctx, type -> giveAll(type, 1) }
                    .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                        .executes { ctx ->
                            val type = ctx.getArgument("type", String::class.java)
                            val amount = IntegerArgumentType.getInteger(ctx, "amount")
                            giveAll(type, amount)
                        }
                    )
                )
            )
    }


    private fun typeArg(defaultExecutor: (CommandContext<CommandSourceStack>, String) -> Int) = Commands.argument("type", com.mojang.brigadier.arguments.StringArgumentType.word())
        .suggests { _, b ->
            val partial = b.remaining.lowercase()
            listOf("Repair1", "Repair2", "Repair3")
                .filter { it.lowercase().startsWith(partial) }
                .forEach(b::suggest)
            b.buildFuture()
        }
        .executes { ctx ->
            val type = ctx.getArgument("type", String::class.java)
            defaultExecutor(ctx, type)
        }

    private fun giveOne(ctx: CommandContext<CommandSourceStack>, type: String, amount: Int): Int {
        val resolver = ctx.getArgument("player", PlayerSelectorArgumentResolver::class.java)
        val target = resolver.resolve(ctx.source).firstOrNull() ?: return 0
        giveRepairGem(target, type, amount)
        return Command.SINGLE_SUCCESS
    }

    private fun giveAll(type: String, amount: Int): Int {
        for (player in Bukkit.getOnlinePlayers()) {
            giveRepairGem(player, type, amount)
        }
        return Command.SINGLE_SUCCESS
    }

    private fun giveRepairGem(player: Player, type: String, quantity: Int) {
        val item = getRepairToken(type, quantity)
        player.give(item)
        Text.msg(player, item.itemMeta?.displayName()?.let {
            "<reset>You have been given</reset> <gold>${quantity}x</gold> ".asComponent().append(it)
        } ?: "???".asComponent())
    }

    private fun getRepairToken(name: String, quantity: Int): ItemStack {
        val itemClass = when (name.lowercase()) {
            "repair1" -> RepairTokenTier1Item::class
            "repair2" -> RepairTokenTier2Item::class
            "repair3" -> RepairTokenTier3Item::class
            else -> RepairTokenTier1Item::class
        }

        val customItem = Registry.CUSTOM_ITEMS.getOrThrow(itemClass)
        return customItem.createItem().second.asQuantity(quantity)
    }
}