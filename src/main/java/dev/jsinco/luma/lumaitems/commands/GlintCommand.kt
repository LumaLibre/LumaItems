package dev.jsinco.luma.lumaitems.commands

import dev.jsinco.luma.lumacore.manager.commands.AbstractCommand
import dev.jsinco.luma.lumacore.manager.commands.CommandInfo
import dev.jsinco.luma.lumacore.manager.modules.AutoRegister
import dev.jsinco.luma.lumacore.manager.modules.RegisterType
import dev.jsinco.luma.lumacore.utility.Text
import io.papermc.paper.datacomponent.DataComponentTypes
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@AutoRegister(RegisterType.COMMAND)
@CommandInfo(
    name = "glint",
    description = "Override an item's enchantment glint",
    usage = "/<command>",
    permission = "lumaitems.command.glint",
    playerOnly = true
)
class GlintCommand : AbstractCommand() {

    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val player = sender as? Player ?: return false
        val item = player.inventory.itemInMainHand
        if (!item.hasItemMeta() || item.itemMeta?.hasEnchants() == false) {
            Text.msg(player, "<red>This item cannot have its glint toggled.")
            return true
        }

        val glintOverrideCurrent = item.getData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE) ?: true
        item.setData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, !glintOverrideCurrent)
        return true
    }

    override fun handleTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String> {
        return listOf()
    }
}