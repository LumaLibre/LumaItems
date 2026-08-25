package dev.lumas.lumaitems.commands.subcommands

import com.mojang.brigadier.Command
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.util.ItemExpiration
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "expiration",
    aliases = ["expiry"],
    description = "Tag the held item with an expiry date, after which it is removed from inventories",
    usage = "/<command> expiration <set <duration>|get|remove>",
    permission = "lumaitems.command.expiration",
    parent = CommandManager::class,
    playerOnly = true
)
class ExpirationCommand : BrigadierSubCommand {

    companion object {
        private val DURATION_SUGGESTIONS = listOf("30m", "1h", "6h", "1d", "2d,6h", "7d")
    }

    override fun buildTree(
        builder: LiteralArgumentBuilder<CommandSourceStack>,
        commands: Commands
    ): LiteralArgumentBuilder<CommandSourceStack> {
        return builder
            .then(Commands.literal("set")
                .then(Commands.argument("duration", StringArgumentType.greedyString())
                    .suggests { _, suggestions ->
                        DURATION_SUGGESTIONS.forEach { suggestions.suggest(it) }
                        suggestions.buildFuture()
                    }
                    .executes { ctx -> set(ctx, StringArgumentType.getString(ctx, "duration")) }
                )
            )
            .then(Commands.literal("get").executes { ctx -> get(ctx) })
            .then(Commands.literal("remove").executes { ctx -> remove(ctx) })
    }

    private fun set(ctx: CommandContext<CommandSourceStack>, rawDuration: String): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0

        val duration = ItemExpiration.parseDuration(rawDuration)
        if (duration == null) {
            player.send("<red>Couldn't read the duration <white>$rawDuration</white>")
            return 0
        }

        val expiresAt = System.currentTimeMillis() + duration.toMillis()
        ItemExpiration.apply(item, expiresAt)
        player.send("Expires in <white>${ItemExpiration.formatSpan(duration.toMillis())}</white> <dark_gray>(${ItemExpiration.formatStamp(expiresAt)})</dark_gray>")
        return Command.SINGLE_SUCCESS
    }

    private fun get(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0

        val expiresAt = ItemExpiration.expiresAt(item)
        if (expiresAt == null) {
            player.send("This item has no expiration")
            return Command.SINGLE_SUCCESS
        }

        val remaining = expiresAt - System.currentTimeMillis()
        if (remaining <= 0) {
            player.send("<red>Expired <white>${ItemExpiration.formatSpan(-remaining)}</white> ago <dark_gray>(${ItemExpiration.formatStamp(expiresAt)})</dark_gray>")
        } else {
            player.send("Expires in <white>${ItemExpiration.formatSpan(remaining)}</white> <dark_gray>(${ItemExpiration.formatStamp(expiresAt)})</dark_gray>")
        }
        return Command.SINGLE_SUCCESS
    }

    private fun remove(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = heldItem(player) ?: return 0

        if (!ItemExpiration.clear(item)) {
            player.send("This item has no expiration")
            return 0
        }
        player.send("Expiration removed")
        return Command.SINGLE_SUCCESS
    }

    private fun heldItem(player: Player): ItemStack? {
        val item = player.inventory.itemInMainHand
        if (item.type.isAir) {
            player.send("Hold an item in your main hand first")
            return null
        }
        return item
    }
}
