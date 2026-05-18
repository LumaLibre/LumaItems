package dev.lumas.lumaitems.commands

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.wrappers.EnumWrappers
import dev.lumas.core.annotation.Autowire
import dev.lumas.core.annotation.CommandMeta
import dev.lumas.core.annotation.Register
import dev.lumas.core.model.command.AbstractCommand
import dev.lumas.core.util.Text
import dev.lumas.lumaitems.hooks.ProtocolLibHook
import dev.lumas.lumaitems.registry.Registry
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Register(Autowire.COMMAND)
@CommandMeta(
    name = "swaphands",
    aliases = ["offhand", "ofh"],
    description = "Command which sends a packet to swap the player's hands",
    usage = "/<command>",
    playerOnly = true
)
class SwapHandsCommand : AbstractCommand() {

    override fun handle(sender: CommandSender, label: String, args: Array<out String>): Boolean {
        val protocolManager = Registry.HOOKS.getOrThrow(ProtocolLibHook::class).getProtocolManager() ?: run {
            Text.msg(sender, "Couldn't find ProtocolLib!")
            return true
        }
        sender as? Player ?: run {
            Text.msg(sender, "Only players can use this command!")
            return true
        }

        val packet = protocolManager.createPacket(PacketType.Play.Client.BLOCK_DIG)
        packet.playerDigTypes.write(0, EnumWrappers.PlayerDigType.SWAP_HELD_ITEMS)
        protocolManager.receiveClientPacket(sender, packet)
        Text.msg(sender, "Swapped hands!")
        return true
    }

    override fun handleTabComplete(sender: CommandSender, label: String, args: Array<out String>): List<String>? {
        return null
    }

}