package dev.lumas.lumaitems.commands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierCommand
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.entity.Player

@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "glint",
    description = "Override an item's enchantment glint",
    usage = "/<command>",
    permission = "lumaitems.command.glint",
    playerOnly = true
)
class GlintCommand : BrigadierCommand() {

    @BrigadierExecutor
    fun run(src: CommandSourceStack) {
        val player = src.sender as Player
        val item = player.inventory.itemInMainHand
        if (!item.hasItemMeta() || item.itemMeta?.hasEnchants() == false) {
            player.send("<red>This item cannot have its glint toggled.")
            return
        }

        val glintOverrideCurrent = item.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) ?: true
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, !glintOverrideCurrent)
    }
}