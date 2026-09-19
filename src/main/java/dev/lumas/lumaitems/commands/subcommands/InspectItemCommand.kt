package dev.lumas.lumaitems.commands.subcommands

import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.BrigadierExecutor
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.brigadier.BrigadierSubCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.commands.CommandManager
import dev.lumas.lumaitems.util.extensions.Executors
import dev.lumas.lumaitems.util.extensions.send
import io.papermc.paper.adventure.PaperAdventure
import io.papermc.paper.command.brigadier.CommandSourceStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtUtils
import net.minecraft.world.item.ItemStack
import org.bukkit.craftbukkit.CraftWorld
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Player

/**
 * Replacement for `/data get entity @s SelectedItem`, which isn't available on Folia based server software
 */
@Register(Autowire.BRIGADIER)
@CommandMeta(
    name = "inspectitem",
    aliases = ["nbt"],
    description = "Show the full data of the item in your main hand",
    usage = "/<command> inspectitem",
    permission = "lumaitems.command.inspectitem",
    parent = CommandManager::class,
    playerOnly = true
)
class InspectItemCommand : BrigadierSubCommand {

    @BrigadierExecutor
    fun run(src: CommandSourceStack) {
        val player = src.sender as Player
        // Inventory must be read on the player's region thread
        val nmsItem = CraftItemStack.asNMSCopy(player.inventory.itemInMainHand)
        if (nmsItem.isEmpty) {
            player.send("You are not holding an item.")
            return
        }
        val registries = (player.world as CraftWorld).handle.registryAccess()

        Executors.async {
            val tag = try {
                ItemStack.CODEC.encodeStart(registries.createSerializationContext(NbtOps.INSTANCE), nmsItem).getOrThrow()
            } catch (e: Exception) {
                player.send("Failed to serialize item: ${e.message}")
                return@async
            }

            val snbt = tag.toString()
            val header = Component.text("${player.name} has the following item data (click to copy): ", NamedTextColor.GRAY)
                .clickEvent(ClickEvent.copyToClipboard(snbt))
                .hoverEvent(Component.text("Click to copy SNBT"))
            Text.msg(player, header.append(PaperAdventure.asAdventure(NbtUtils.toPrettyComponent(tag))))
        }
    }
}
