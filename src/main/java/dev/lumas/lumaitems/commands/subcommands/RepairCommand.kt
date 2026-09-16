package dev.lumas.lumaitems.commands.subcommands

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "repair",
    description = "Repair the held item or every item in your inventory",
    usage = "/<command> repair [all]",
    permission = "lumaitems.command.repair",
    parent = CommandManager::class,
    playerOnly = true
)
class RepairCommand : BrigadierSubCommand {

    override fun buildTree(
        builder: LiteralArgumentBuilder<CommandSourceStack>,
        commands: Commands
    ): LiteralArgumentBuilder<CommandSourceStack> {
        return builder
            .executes(::repairHeld)
            .then(Commands.literal("all").executes(::repairAll))
    }

    private fun repairHeld(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val item = player.inventory.itemInMainHand
        if (item.type.isAir) {
            player.send("Hold an item in your main hand first.")
            return 0
        }
        if (!repair(item)) {
            player.send("This item doesn't need repairing.")
            return 0
        }
        player.send("Repaired your held item.")
        return Command.SINGLE_SUCCESS
    }

    private fun repairAll(ctx: CommandContext<CommandSourceStack>): Int {
        val player = ctx.source.sender as Player
        val repaired = player.inventory.contents.count { it != null && repair(it) }
        if (repaired == 0) {
            player.send("No items needed repairing.")
            return 0
        }
        player.send("Repaired $repaired item(s).")
        return Command.SINGLE_SUCCESS
    }

    private fun repair(item: ItemStack): Boolean {
        val meta = item.itemMeta as? Damageable ?: return false
        if (!meta.hasDamage() || meta.damage <= 0) return false
        meta.damage = 0
        item.itemMeta = meta
        return true
    }
}
