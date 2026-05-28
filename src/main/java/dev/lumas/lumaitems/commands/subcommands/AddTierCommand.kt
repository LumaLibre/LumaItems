package dev.lumas.lumaitems.commands.subcommands

import com.mojang.brigadier.arguments.StringArgumentType
import dev.lumas.core.annotation.Argument
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.ArgumentTypeProvider
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.commands.providers.GreedyStringProvider
import dev.lumas.lumaitems.model.item.ItemFactory
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import org.bukkit.entity.Player

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "tier",
    description = "Add a tier to an item",
    usage = "/<command> tier <text>",
    permission = "lumaitems.command.tier",
    playerOnly = true,
    parent = CommandManager::class
)
class AddTierCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack, @Argument(value = "text", provider = GreedyStringProvider::class) tier: String) {
        val player = src.sender as Player
        val item = player.inventory.itemInMainHand

        if (item.type.isAir) {
            player.send("You must be holding an item to add a tier!")
            return
        }

        item.editMeta { meta ->
            val lore: MutableList<Component> = meta?.lore() ?: mutableListOf()
            lore.addAll(Text.mmlNoItalic(ItemFactory.TIER_FORMAT.map { it.format(tier) }))
            meta.lore(lore)
        }

        player.send("Successfully added tier to item.")
    }
}